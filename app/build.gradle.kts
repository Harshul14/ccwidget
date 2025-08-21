plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.developer.harshul.ccwidget"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.developer.harshul.ccwidget"
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
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all {
                // Configure additional test behaviors here if needed
            }
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    testImplementation(libs.ext.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
//    implementation("com.google.android.material:material:1.11.0")
//    implementation("androidx.core:core:ktx:1.13.0")
//    implementation("androidx.appcompat:appcompat:1.7.1")
//    implementation("androidx.coordinatorlayout:coordinatorlayout:1.3.0")
//    testImplementation("junit:junit:4.13.2")
//    testImplementation("org.mockito:mockito-core:5.4.0")
//    testImplementation("org.mockito:mockito-inline:5.2.0") // For mocking final classes like Context
//    testImplementation("androidx.test:core:1.5.0") // Provides Android context for tests
//    testImplementation("org.robolectric:robolectric:4.10.3") // For resources and some Android components
//    testImplementation("org.json:json:20231013")


    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.4.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation ("org.robolectric:robolectric:4.11.1")

    // Android Testing
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.test:runner:1.5.2")
    androidTestImplementation ("androidx.test:rules:1.5.0")
    androidTestImplementation ("org.mockito:mockito-android:5.4.0")

    // JSON Testing
    testImplementation ("org.json:json:20230618")

    // Additional testing utilities
    testImplementation ("androidx.test:core:1.5.0")
    testImplementation ("androidx.arch.core:core-testing:2.2.0")
}