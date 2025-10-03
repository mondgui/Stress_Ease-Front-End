import org.gradle.kotlin.dsl.testImplementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.stressease"
    compileSdk = 35

    android {
        // ... other settings like compileSdk, defaultConfig ...

        buildFeatures {
            viewBinding = true
        }
    }


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
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // RecyclerView
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



    // Add this line for the PDF Viewer
    implementation(libs.android.pdf.viewer)

}