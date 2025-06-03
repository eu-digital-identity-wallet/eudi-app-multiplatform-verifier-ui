package eu.europa.ec.euidi.verifier.preferences

import io.ktor.utils.io.core.toByteArray
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreCrypto.CCCrypt
import platform.CoreCrypto.kCCAlgorithmAES
import platform.CoreCrypto.kCCBlockSizeAES128
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCOptionPKCS7Padding
import platform.CoreCrypto.kCCSuccess
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import platform.posix.size_tVar

actual object Crypto {
    private const val KEY_ALIAS = "secret"
    private const val KEY_SIZE = 32      // 256 bits
    private const val IV_SIZE = 16       // AES block size (128 bits)
    private val keychainSettings = KeychainSettings("eu.europa.ec.euidi.verifier")

    private fun getKey(): ByteArray {
        val key = keychainSettings.getStringOrNull(KEY_ALIAS)?.toByteArray() ?: createKey()
        return key
    }

    @OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
    private fun createKey(): ByteArray {
        val key = ByteArray(KEY_SIZE)
        key.usePinned { pinned ->
            if (SecRandomCopyBytes(kSecRandomDefault, KEY_SIZE.toULong(), pinned.addressOf(0)) != 0) {
                throw Exception("Failed to generate random key")
            }
        }
        keychainSettings.putString(KEY_ALIAS, key.decodeToString())
        return key
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun encrypt(bytes: ByteArray): ByteArray {
        val key = getKey()
        val iv = ByteArray(IV_SIZE)
        iv.usePinned { pinned ->
            if (SecRandomCopyBytes(kSecRandomDefault, IV_SIZE.toULong(), pinned.addressOf(0)) != 0) {
                throw Exception("Failed to generate IV")
            }
        }
        val outputBufferSize = bytes.size + kCCBlockSizeAES128.toInt()
        val encrypted = ByteArray(outputBufferSize)
        val numBytesEncrypted = memScoped {
            val numBytesEncryptedVar = alloc<UIntVar>()
            encrypted.usePinned { encryptedPinned ->
                bytes.usePinned { bytesPinned ->
                    key.usePinned { keyPinned ->
                        iv.usePinned { ivPinned ->
                            val cryptStatus = CCCrypt(
                                kCCEncrypt,
                                kCCAlgorithmAES,
                                kCCOptionPKCS7Padding,
                                keyPinned.addressOf(0),
                                KEY_SIZE.toULong(),
                                ivPinned.addressOf(0),
                                bytesPinned.addressOf(0),
                                bytes.size.toULong(),
                                encryptedPinned.addressOf(0),
                                outputBufferSize.toULong(),
                                numBytesEncryptedVar.ptr.reinterpret<size_tVar>()
                            )
                            if (cryptStatus != kCCSuccess) {
                                throw Exception("Encryption failed with status: $cryptStatus")
                            }
                            numBytesEncryptedVar.value.toInt()
                        }
                    }
                }
            }
        }
        val result = ByteArray(IV_SIZE + numBytesEncrypted)
        iv.copyInto(result, destinationOffset = 0, startIndex = 0, endIndex = IV_SIZE)
        encrypted.copyInto(result, destinationOffset = IV_SIZE, startIndex = 0, endIndex = numBytesEncrypted)
        return result
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun decrypt(bytes: ByteArray): ByteArray {
        if (bytes.size < IV_SIZE) throw Exception("Invalid ciphertext: too short")
        val key = getKey()
        val iv = bytes.copyOfRange(0, IV_SIZE)
        val cipherText = bytes.copyOfRange(IV_SIZE, bytes.size)
        val outputBufferSize = cipherText.size + kCCBlockSizeAES128.toInt()
        val decrypted = ByteArray(outputBufferSize)
        val numBytesDecrypted = memScoped {
            val numBytesDecryptedVar = alloc<UIntVar>()
            decrypted.usePinned { decryptedPinned ->
                cipherText.usePinned { cipherPinned ->
                    key.usePinned { keyPinned ->
                        iv.usePinned { ivPinned ->
                            val cryptStatus = CCCrypt(
                                kCCDecrypt,
                                kCCAlgorithmAES,
                                kCCOptionPKCS7Padding,
                                keyPinned.addressOf(0),
                                KEY_SIZE.toULong(),
                                ivPinned.addressOf(0),
                                cipherPinned.addressOf(0),
                                cipherText.size.toULong(),
                                decryptedPinned.addressOf(0),
                                outputBufferSize.toULong(),
                                numBytesDecryptedVar.ptr.reinterpret()
                            )
                            if (cryptStatus != kCCSuccess) {
                                throw Exception("Decryption failed with status: $cryptStatus")
                            }
                            numBytesDecryptedVar.value.toInt()
                        }
                    }
                }
            }
        }
        return decrypted.copyOf(numBytesDecrypted)
    }
}