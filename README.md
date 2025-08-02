# EUDI Multiplatform Verifier Application

:heavy_exclamation_mark: **Important!** Before you proceed, please read
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
- iOS 16 or higher

### Prerequisites

You can download the application (apk file) through GitHub releases [here](https://github.com/eu-digital-identity-wallet/eudi-app-multiplatform-verifier-ui/releases)

Alternatively, you can build it yourself using Xcode for iOS or Android Studio for Android.

### Proximity flow

1. Log in to the EUDI Wallet app.
2. You will be on the "Home" tab of the "Dashboard" screen.
3. Tap the "Authenticate" button on the first informative card. A modal with two options will appear.
4. Select "In person".
5. You will be prompted to enable Bluetooth (if it is not already enabled) and grant the necessary permissions for the app to use it (if you have not already done so).
6. In the EUDI Verifier app, select the document (e.g., PID, MDL, etc.) you want to request from the EUDI Wallet app.
7. Scan the presented QR code with the EUDI Verifier app.
8. The EUDI Wallet app's "Request" screen will load. Here, you can select or deselect which attributes to share with the EUDI Verifier app. You must choose at least one attribute to proceed.
9. Tap "Share".
10. Enter the PIN you set up during the initial steps.
11. Upon successful authentication, tap "Close".
12. The EUDI Verifier app will receive the data you’ve chosen to share and display them to you.
13. In the EUDI Wallet app, you will be returned to the "Home" tab of the "Dashboard" screen. The flow is now complete.

## Application configuration

The EUDI Verifier App utilizes a ConfigProvider (verifierApp -> commonMain -> domain -> config -> ConfigProvider.kt) to define which credential types and claims are supported, as well as which document modes (FULL, CUSTOM) are available for each credential type.
This approach allows the app to retrieve and update document configuration dynamically.

You can configure the supported documents and claims by:

•	Adding a new attestation type and updating supportedDocuments with its respective list of claims:
```Kotlin
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

•	Specifying document modes (e.g., only FULL for some docs) and update getDocumentModes():
```Kotlin
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

## Disclaimer

The released software is an initial development release version: 
-  The initial development release is an early endeavor reflecting the efforts of a short time-boxed period, and by no means can be considered as the final product.  
-  The initial development release may be changed substantially over time and might introduce new features but also may change or remove existing ones, potentially breaking compatibility with your existing code.
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

Copyright (c) 2025 European Commission

Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
except in compliance with the Licence.

You may obtain a copy of the Licence at:
https://joinup.ec.europa.eu/software/page/eupl

Unless required by applicable law or agreed to in writing, software distributed under 
the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF 
ANY KIND, either express or implied. See the Licence for the specific language 
governing permissions and limitations under the Licence.
