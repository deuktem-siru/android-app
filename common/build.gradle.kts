plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.deuktemsiru.common"
    compileSdk { version = release(36) { minorApiLevel = 1 } }
    defaultConfig { minSdk = 29 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.androidx.constraintlayout)
    api(libs.androidx.fragment.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.security.crypto)
    api(platform(libs.firebase.bom))
    api(libs.firebase.messaging)
    api("com.squareup.retrofit2:retrofit:2.11.0")
    api("com.squareup.retrofit2:converter-gson:2.11.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    api("com.kakao.sdk:v2-user:2.21.0")
    testImplementation(libs.junit)
}
