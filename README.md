# EUDI Multiplatform Verifier Application

:heavy_exclamation_mark: **Important!** Before you proceed, please read
the [EUDI Wallet Reference Implementation project description](https://github.com/eu-digital-identity-wallet/.github/blob/main/profile/reference-implementation.md)

----

## Table of contents

* [Overview](#overview)
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

This repository contains the source code for the multi-platform app, while core cryptographic and credential-handling libraries are used as external dependencies.

## How to use the application

### Minimum device requirements

- API level 29 (Android 10) or higher.
- iOS 16 or higher

### Prerequisites

You can download the application (apk file) through GitHub releases [here](https://github.com/eu-digital-identity-wallet/eudi-app-multiplatform-verifier-ui/releases)

## Application configuration

// TODO

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

Copyright (c) 2023 European Commission

Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
except in compliance with the Licence.

You may obtain a copy of the Licence at:
https://joinup.ec.europa.eu/software/page/eupl

Unless required by applicable law or agreed to in writing, software distributed under 
the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF 
ANY KIND, either express or implied. See the Licence for the specific language 
governing permissions and limitations under the Licence.
