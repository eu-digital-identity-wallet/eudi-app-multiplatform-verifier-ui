/*
 * Copyright (c) 2025 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinParcelize)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kover)
}

kotlin {
    val basePackage = "eu.europa.ec.euidi.verifier"
    val parcelizeAnnotationPath = "$basePackage.presentation.utils"

    androidLibrary {

        namespace = "eu.europa.ec.euidi.verifier.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true

        withHostTestBuilder { }.configure { }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-P",
                "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=$parcelizeAnnotationPath.CommonParcelize"
            )
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "VerifierApp"
            isStatic = true
            freeCompilerArgs += listOf(
                "-Xbinary=bundleId=eu.europa.ec.euidi.verifier.shared"
            )
        }
    }

    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.core)
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(libs.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            implementation(libs.backhandler.cmp)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            api(libs.koin.core)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.annotations)

            api(libs.androidx.datastore.preferences.core)
            api(libs.androidx.datastore.core.okio)
            implementation(libs.okio)

            implementation(libs.ktor.client.core)
            implementation(libs.qr.kit)

            implementation(libs.moko.permissions.compose)
            implementation(libs.moko.permissions.ble)
            implementation(libs.moko.permissions.location)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }

    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    ksp {
        arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
    }
}

kotlin.sourceSets
    .matching { it.name.startsWith("ios") }
    .all {
        languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
    }


dependencies {
    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
    add("kspIosX64", libs.koin.ksp.compiler)
    add("kspIosArm64", libs.koin.ksp.compiler)
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
}

kover {
    reports {
        total {
            html { onCheck = false }
            xml { onCheck = false }
            filters {
                excludes {
                    classes(
                        "*BuildConfig*",
                        "org.koin*",
                    )
                }
            }
        }
    }
}

afterEvaluate {

    val kspCommon = tasks.findByName("kspCommonMainKotlinMetadata")
        ?: tasks.findByName("kspCommonMainMetadata")
        ?: return@afterEvaluate

    tasks.matching { it.name.startsWith("kspKotlin") }.configureEach {
        dependsOn(kspCommon)
    }
    tasks.matching { it.name.startsWith("compile") }.configureEach {
        dependsOn(kspCommon)
    }
}