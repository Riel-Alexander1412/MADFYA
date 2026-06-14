plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.mobile.madfya"
    compileSdk = 35 // Cleaned up to match standard SDK compile target

    defaultConfig {
        applicationId = "com.mobile.madfya"
        minSdk = 28
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core AndroidX and UI Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.recyclerview)

    // Navigation Components
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Architecture Lifecycles & Persistence
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Google Location Services & Maps API
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)

    // Third Party UI Assets & Plugins
    implementation(libs.mpandroidchart)
    implementation(libs.glide)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    // Quality Control Testing Packages
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}