plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val releaseStoreFile = System.getenv("ETA_RELEASE_STORE_FILE")
val releaseStorePassword = System.getenv("ETA_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = System.getenv("ETA_RELEASE_KEY_ALIAS")
val releaseKeyPassword = System.getenv("ETA_RELEASE_KEY_PASSWORD")
val hasReleaseSigning = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { !it.isNullOrBlank() }

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// CI builds may add the commit SHA as SemVer build metadata; local and release builds omit it.
val buildMetadata = providers.gradleProperty("eta.buildMetadata").orNull?.takeIf { it.isNotBlank() }
require(buildMetadata == null || buildMetadata.matches(Regex("[0-9A-Za-z.-]+"))) {
    "eta.buildMetadata may contain only letters, numbers, dots, and hyphens"
}
val buildMetadataSuffix = buildMetadata?.let { "+$it" }.orEmpty()

android {
    namespace = "io.github.mangi.eta"
    compileSdk = 37
    ndkVersion = libs.versions.ndk.get()

    defaultConfig {
        applicationId = "io.github.mangi.eta"
        minSdk = 33
        targetSdk = 37
        // versionCode format: yyyyMMdd plus a two-digit daily sequence (starting at 01).
        versionCode = 2026101401
        versionName = "3.3.1"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(requireNotNull(releaseStoreFile))
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            // Include the build type in the version name to distinguish debug APKs.
            versionNameSuffix = "-debug" + buildMetadataSuffix
            isMinifyEnabled = false
            isPseudoLocalesEnabled = true
        }
        release {
            versionNameSuffix = buildMetadataSuffix
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    buildFeatures {
        buildConfig = false
        compose = true
    }

    androidResources {
        localeFilters += listOf("en", "sq", "b+zh+Hans", "b+zh+Hant")
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
            keepDebugSymbols += setOf("**/libproot_exec.so", "**/libproot_loader.so", "**/libeta_pty.so")
            // GPL Go executable is packaged as a native library for Android's executable mount.
            keepDebugSymbols += "**/libeta_opencode2api.so"
        }
        resources {
            // Preserve Xposed module declarations in release builds.
            merges += "META-INF/xposed/*"
            // Exclude only conflicting signature/version metadata.
            excludes += "META-INF/*.kotlin_module"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
        // The date-based versionCode remains below Android's maximum through 2099.
        disable += "HighAppVersionCode"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        // Robolectric on newer JDKs needs access to internal APIs; only test JVMs receive these flags.
        unitTests.all {
            it.jvmArgs(
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "--add-opens=java.base/java.io=ALL-UNNAMED",
                "--add-opens=java.base/java.net=ALL-UNNAMED",
                "--add-opens=java.base/java.security=ALL-UNNAMED",
                "--add-opens=java.base/java.text=ALL-UNNAMED",
                "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
                "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
                "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
                "--enable-native-access=ALL-UNNAMED",
            )
        }
    }
}

androidComponents {
    onVariants { variant ->
        // Keep APK names based on versionName, including the build-type suffix.
        variant.outputs.forEach { output ->
            output.outputFileName.set(output.versionName.map { "Eta-v$it.apk" })
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)
    implementation(libs.jsoup)
    implementation(libs.commons.compress)
    implementation(libs.xz)
    compileOnly(libs.libxposed.api)
    // RemotePreferences bridge for UI writes and Xposed hook reads.
    implementation(libs.libxposed.service)
    implementation(libs.dexkit)
    implementation(libs.miuix.ui)
    implementation(libs.miuix.blur)
    implementation(libs.miuix.nav)
    implementation(libs.miuix.preference)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.navigationevent)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)
    // GFM parser; chat rendering is implemented in ui/markdown.
    implementation(libs.intellij.markdown)
    implementation(libs.hidden.api.bypass)

    // DataStore for structured provider/model settings and selected IDs.
    implementation(libs.datastore.preferences)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // OkHttp supports SSE.
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.json)
    testImplementation(libs.room.testing)
    testImplementation(libs.robolectric)
}
