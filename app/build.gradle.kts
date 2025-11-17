plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.lumiapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.lumiapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.functions)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation (libs.firebase.auth)
    implementation ("com.google.firebase:firebase-firestore")
    implementation("io.agora.rtc:full-sdk:4.6.0")
    implementation ("io.getstream:stream-chat-android-ui-components:6.27.0")
    implementation ("io.getstream:stream-chat-android-compose:6.27.0")
    implementation ("io.getstream:stream-chat-android-offline:6.27.0")

}