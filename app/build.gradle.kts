plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.android.application")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.maincameleon"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.maincameleon"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation libs.androidx.core.ktx
    implementation libs.androidx.appcompat
    implementation libs.material
    implementation libs.androidx.constraintlayout
    implementation libs.androidx.lifecycle.livedata.ktx
    implementation libs.androidx.lifecycle.viewmodel.ktx
    implementation libs.androidx.navigation.fragment.ktx
    implementation libs.androidx.navigation.ui.ktx
    implementation libs.firebase.storage.ktx
    testImplementation libs.junit
    androidTestImplementation libs.androidx.junit
    androidTestImplementation libs.androidx.espresso.core

    // Firebase BOM for consistent Firebase dependency versions
    implementation platform("com.google.firebase:firebase-bom:33.5.1")
    implementation libs.firebase.auth
    implementation libs.firebase.firestore.ktx
    implementation libs.firebase.storage

    // Additional AndroidX dependencies
    implementation libs.androidx.camera.camera2
    implementation libs.androidx.camera.lifecycle
    implementation libs.androidx.camera.view

    // Other dependencies
    implementation libs.guava
    implementation libs.hilt.android
    kapt libs.hilt.android.compiler

    // Glide
    implementation libs.glide

    // Apply Google services plugin for Firebase
    apply plugin: 'com.google.gms.google-services'
}
