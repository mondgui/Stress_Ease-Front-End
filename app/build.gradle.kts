import org.gradle.kotlin.dsl.testImplementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.stressease"
    compileSdk = 35

    defaultConfig {

        applicationId = "com.example.stressease"
        minSdk = 35
        targetSdk = 35
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
    buildFeatures{
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    configurations.all {
        exclude(group = "com.android.support")
        exclude(group = "android.support")
    }
}

dependencies {
    implementation(libs.core.ktx.v1120)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.androidx.activity.ktx.v182)
    implementation(libs.constraintlayout.v220)
    implementation(libs.mhiew.android.pdf.viewer)
    implementation(libs.androidx.recyclerview)

    // Retrofit & OkHttp for API calls
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Charts (Reports / Graphs)
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.material3)
    implementation(libs.material.v1120)
    // Animations
    implementation(libs.lottie)

    // Kotlin Coroutines (for async network calls)
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)

    //Firebase
    // Firebase BOM (manages versions)
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))

// Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

// Firebase Firestore (for reports, moods, chat etc.)
    implementation("com.google.firebase:firebase-firestore")

// (Optional) Realtime Database
    implementation("com.google.firebase:firebase-database")

// (Optional) Analytics
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.gms:play-services-auth:20.7.0")



}
