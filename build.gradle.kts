import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.google.services) apply false
}

subprojects {
    plugins.withId("com.android.application") {
        if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")
        val properties = Properties().apply {
            rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
            project.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
        }
        extra["localProperties"] = properties
        val hasSigning = listOf("KEYSTORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD")
            .all { !properties.getProperty(it).isNullOrBlank() } && file("release.keystore").exists()
        extensions.configure<ApplicationExtension> {
            compileSdk { version = release(36) { minorApiLevel = 1 } }
            defaultConfig {
                minSdk = 29
                targetSdk = 36
                versionCode = 1
                versionName = "1.0"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                buildConfigField("String", "BASE_URL", "\"${properties.getProperty("BACKEND_BASE_URL", "http://10.0.2.2:8080/")}\"")
                manifestPlaceholders["kakao_app_key"] = properties.getProperty("KAKAO_NATIVE_APP_KEY", "")
                buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"${properties.getProperty("KAKAO_NATIVE_APP_KEY", "")}\"")
            }
            if (hasSigning) signingConfigs.create("release") {
                storeFile = file("release.keystore")
                storePassword = properties.getProperty("KEYSTORE_PASSWORD")
                keyAlias = properties.getProperty("KEY_ALIAS")
                keyPassword = properties.getProperty("KEY_PASSWORD")
            }
            buildTypes.getByName("release") {
                isMinifyEnabled = false
                if (hasSigning) signingConfig = signingConfigs.getByName("release")
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            buildFeatures { viewBinding = true; buildConfig = true }
        }
    }
}
