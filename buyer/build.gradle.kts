import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = extra["localProperties"] as Properties

android {
    namespace = "com.example.deuktemsiru_buyer"
    defaultConfig {
        applicationId = "com.example.deuktemsiru_buyer"
        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY", "")
        buildConfigField("String", "TMAP_API_KEY", "\"${localProperties.getProperty("TMAP_API_KEY", "")}\"")
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(project(":common"))
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.gridlayout)
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.zxing:core:3.5.3")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
