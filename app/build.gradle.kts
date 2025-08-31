plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.developer.harshul.pinvoke"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.developer.harshul.pinvoke"
        minSdk = 30
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
//    implementation("com.google.android.material:material:1.11.0")
//    implementation("androidx.core:core:ktx:1.13.0")
//    implementation("androidx.appcompat:appcompat:1.7.1")
//    implementation("androidx.coordinatorlayout:coordinatorlayout:1.3.0")
}