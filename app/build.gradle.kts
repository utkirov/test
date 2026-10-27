plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Release signing: set BOTIR_KEYSTORE (path), BOTIR_KS_PASS, BOTIR_KEY_ALIAS, BOTIR_KEY_PASS.
// Without them the build is signed with the debug key so it still installs.
val ksPath: String? = System.getenv("BOTIR_KEYSTORE")?.takeIf { it.isNotBlank() && file(it).exists() }

android {
    namespace = "uz.botir.game"
    compileSdk = 36

    defaultConfig {
        applicationId = "uz.botir.game"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"
    }

    signingConfigs {
        if (ksPath != null) create("upload") {
            storeFile = file(ksPath)
            storePassword = System.getenv("BOTIR_KS_PASS")
            keyAlias = System.getenv("BOTIR_KEY_ALIAS")
            keyPassword = System.getenv("BOTIR_KEY_PASS")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (ksPath != null) signingConfigs.getByName("upload") else signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}
