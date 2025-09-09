plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.calikot.mysavingquest"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.calikot.mysavingquest"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.espresso.core)
    //noinspection UseTomlInstead
    implementation("androidx.navigation:navigation-compose:2.9.3")
    //noinspection UseTomlInstead
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.1"))
    //noinspection UseTomlInstead
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    //noinspection UseTomlInstead
    implementation("io.ktor:ktor-client-android:3.2.3")
    //noinspection UseTomlInstead
    implementation("com.google.dagger:hilt-android:2.51.1")
    //noinspection UseTomlInstead
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    //noinspection UseTomlInstead
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
