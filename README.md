# EUDI Multiplatform Verifier Application

[![License: EUPL 1.2](https://img.shields.io/badge/License-EUPL%201.2-blue.svg)](https://joinup.ec.europa.eu/software/page/eupl)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.11-4285F4.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Android API](https://img.shields.io/badge/Android%20API-29%2B-3DDC84.svg?logo=android)](https://developer.android.com/about/versions/10)
[![iOS](https://img.shields.io/badge/iOS-18.6%2B-000000.svg?logo=apple)](https://developer.apple.com/ios/)
[![SonarCloud](https://img.shields.io/badge/SonarCloud-enabled-F3702A.svg?logo=sonarcloud)](https://sonarcloud.io)
[![Dependency Check](https://img.shields.io/badge/Dependency--Check-enabled-005A9C.svg)](https://owasp.org/www-project-dependency-check/)
[![Gitleaks](https://img.shields.io/badge/Gitleaks-enabled-orange.svg)](https://github.com/gitleaks/gitleaks)

⚠️ **Important!** Before you proceed, please read
the [EUDI Wallet Reference Implementation project description](https://github.com/eu-digital-identity-wallet/.github/blob/main/profile/reference-implementation.md)

----

## Table of contents

* [Overview](#overview)
* [Important things to know](#important-things-to-know)
* [How to use the application](#how-to-use-the-application)
* [Application configuration](#application-configuration)
* [Disclaimer](#disclaimer)
* [How to contribute](#how-to-contribute)
* [License](#license)

## Overview

The EUDI Verifier App is a cross-platform (iOS and Android) reference implementation for ISO 18013-5 proximity-based credential verification. Built with Kotlin Multiplatform (KMP/CMP), the app provides a unified codebase for business logic, leveraging platform-native libraries to ensure compliance with the latest standards and security requirements.

The EUDI Verifier App enables organizations and relying parties to:

1. Read and verify ISO 18013-5 compliant mobile credentials (mDL, PID, etc.) over proximity channels (NFC/BLE).
2. Support secure, privacy-preserving credential presentation flows, both for in-person and potentially remote scenarios.
3. Demonstrate modular, reusable architecture by utilizing platform-specific low-level libraries, orchestrated by a shared multiplatform business logic layer.
4. Provide an accessible, extensible codebase for pilots, research, and real-world integration projects targeting digital identity verification.

This repository contains the source code for the multi-platform app, while credential-handling libraries are used as external dependencies.

## Important things to know

Currently, the project supports building both Android and iOS applications. However, only the Android version is fully operational. The iOS version builds the user interface, but no actions are functional, as the ISO 18013-5 library has not yet been implemented. Support for this functionality is planned for a future release.

## How to use the application

### Minimum device requirements

- API level 29 (Android 10) or higher.
- iOS 18.6 or higher

### Prerequisites

You can download the application (APK file) through GitHub releases [here](https://github.com/eu-digital-identity-wallet/eudi-app-multiplatform-verifier-ui/releases)

Alternatively, you can build it yourself using Xcode for iOS or Android Studio for Android.

### Build from source

#### Required tools

- **JDK 17** (the Gradle toolchain is pinned to 17)
- **Android Studio** (latest stable) — for the Android app
- **Xcode 16 or newer** — for the iOS app (note that iOS is currently UI-only)
- **Git**

#### Clone

```bash
git clone https://github.com/eu-digital-identity-wallet/eudi-app-multiplatform-verifier-ui.git
cd eudi-app-multiplatform-verifier-ui
```

#### Android

The Android app ships with two product flavors on the `environment` dimension:

| Flavor | Application ID | Label |
|---|---|---|
| `Dev` | `eu.europa.ec.euidi.verifier.dev` | `(Dev) EUDI Verifier` |
| `Public` | `eu.europa.ec.euidi.verifier` | `EUDI Verifier` |

Common Gradle commands (use `gradlew.bat` on Windows):

```bash
# Build a debug APK with the Dev flavor
./gradlew :androidVerifierApp:assembleDevDebug

# Build a release APK with the Public flavor (requires signing config, see below)
./gradlew :androidVerifierApp:assemblePublicRelease

# Run checks on the shared KMP module
./gradlew :verifierApp:check
```

Release builds are signed with the keystore at the repository root (`sign`). Provide the credentials either via `local.properties`:

```properties
androidKeyAlias=<your-alias>
androidKeyPassword=<your-password>
```

…or via environment variables `ANDROID_KEY_ALIAS` and `ANDROID_KEY_PASSWORD`.

#### iOS

Open the Xcode project and build / run from there:

```bash
open iosVerifierApp/iosVerifierApp.xcodeproj
```

Xcode will trigger the KMP framework build for the shared `verifierApp` module on first launch. As noted above, the iOS target currently renders the UI but does not yet perform proximity verification.

### Proximity flow

The verification flow involves both apps: the user holding the **EUDI Wallet** initiates a presentation, and the **EUDI Verifier** reads it. The steps below assume you already have an [EUDI Wallet](https://github.com/eu-digital-identity-wallet) installed and set up alongside this Verifier app.

#### On the EUDI Wallet app

1. Log in to the EUDI Wallet app.
2. You will be on the "Home" tab of the "Dashboard" screen.
3. Tap the "Authenticate" button on the first informative card. A modal with two options will appear.
4. Select "In person".
5. You will be prompted to enable Bluetooth (if it is not already enabled) and grant the necessary permissions for the app to use it (if you have not already done so). The Wallet will then present a QR code.

#### On the EUDI Verifier app

6. Select the document (e.g., PID, MDL, etc.) you want to request from the EUDI Wallet app.
7. Scan the QR code presented by the Wallet.

#### Back on the EUDI Wallet app

8. The "Request" screen will load. Here, you can select or deselect which attributes to share with the EUDI Verifier app. You must choose at least one attribute to proceed.
9. Tap "Share".
10. Enter the PIN you set up during the initial steps.
11. Upon successful authentication, tap "Close". You will be returned to the "Home" tab of the "Dashboard" screen.

#### Back on the EUDI Verifier app

12. The EUDI Verifier app will receive the data you've chosen to share and display it to you. The flow is now complete.

## Application configuration

The EUDI Verifier App utilizes a `ConfigProvider` ([verifierApp/src/commonMain/kotlin/eu/europa/ec/euidi/verifier/domain/config/ConfigProvider.kt](verifierApp/src/commonMain/kotlin/eu/europa/ec/euidi/verifier/domain/config/ConfigProvider.kt)) to define which credential types and claims are supported, as well as which document modes (FULL, CUSTOM) are available for each credential type.
This approach allows the app to retrieve and update document configuration dynamically.

You can configure the supported documents and claims by:

- Adding a new attestation type and updating supportedDocuments with its respective list of claims:

    ```kotlin
    sealed interface AttestationType {
        data object YourDocument : AttestationType {
    
            override val namespace: String
                get() = "your_namespace"
    
            override val docType: String
                get() = "your_doctype"
        }
    }
    
    val supportedDocuments = SupportedDocuments(
        mapOf(
            AttestationType.YourDocument to listOf(
                ClaimItem("your_claim_1"),
                ClaimItem("your_claim_2")
            )
        )
    )
    ```

- Specifying document modes (e.g., only FULL for some docs) and update getDocumentModes():

    ```kotlin
    enum class DocumentMode(val displayName: String) {
        FULL(displayName = "Full"),
        CUSTOM(displayName = "Custom")
    }
    
    fun getDocumentModes(attestationType: AttestationType): List<DocumentMode> {
        return when (attestationType) {
            AttestationType.YourDocument -> listOf(DocumentMode.FULL, DocumentMode.CUSTOM)
        }
    }
    ```

The EUDI Verifier App also validates documents against trusted certificate authorities. The repository ships with PEM-encoded PID issuer trust anchors for **CZ, EE, EU, LU, NL, PT, and UT** under [verifierApp/src/commonMain/composeResources/files/certs](verifierApp/src/commonMain/composeResources/files/certs).

- To configure your own trust anchors, place PEM-encoded certificate files under:
    ```
    verifierApp/src/commonMain/composeResources/files/certs
    ```

- Then update `getCertificates()` to load them:
    ```kotlin
    override suspend fun getCertificates(): List<String> = listOf(
        Res.readBytes("files/certs/your_trust_anchor.pem").decodeToString()
    )
    ```

## Disclaimer

The released software is an initial development release version: 
-  The initial development release is an early endeavor reflecting the efforts of a short time-boxed period, and by no means can it be considered the final product.  
-  The initial development release may be changed substantially over time and might introduce new features, but also may change or remove existing ones, potentially breaking compatibility with your existing code.
-  The initial development release is limited in functional scope.
-  The initial development release may contain errors or design flaws and other problems that could cause system or other failures and data loss.
-  The initial development release has reduced security, privacy, availability, and reliability standards relative to future releases. This could make the software slower, less reliable, or more vulnerable to attacks than mature software.
-  The initial development release is not yet comprehensively documented. 
-  Users of the software must perform sufficient engineering and additional testing to properly evaluate their application and determine whether any of the open-sourced components are suitable for use in that application.
-  We strongly recommend not putting this version of the software into production use.
-  Only the latest version of the software will be supported

## How to contribute

We welcome contributions to this project. To ensure that the process is smooth for everyone
involved, follow the guidelines found in [CONTRIBUTING.md](CONTRIBUTING.md).

## License

### License details

Copyright (c) 2026 European Commission

Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
except in compliance with the Licence.

You may obtain a copy of the Licence at:
https://joinup.ec.europa.eu/software/page/eupl

Unless required by applicable law or agreed to in writing, software distributed under 
the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF 
ANY KIND, either express or implied. See the Licence for the specific language 
governing permissions and limitations under the Licence.
