plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.deuktemsiru_seller"
    defaultConfig {
        applicationId = "com.example.deuktemsiru_seller"
    }
}

dependencies {
    implementation(project(":common"))
    implementation(libs.androidx.activity)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
