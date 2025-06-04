package eu.europa.ec.euidi.verifier.preferences

expect object Crypto {
    fun encrypt(bytes: ByteArray): ByteArray

    fun decrypt(bytes: ByteArray): ByteArray
}