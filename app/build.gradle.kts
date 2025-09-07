plugins {
    alias(libs.plugins.androidApplication)
    id("jacoco")
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
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isTestCoverageEnabled = true
        }
        release {
            isMinifyEnabled = true
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
        unitTests.all {
            it.jvmArgs("-noverify")
            it.systemProperty("robolectric.enabledSdks", "28")
            it.systemProperty("robolectric.offline", "true")
        }
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true
    }

    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module"
            )
        }
    }
}

dependencies {
    // Core
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Extra UI support
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.fragment:fragment:1.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")

    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.1")

    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito:mockito-android:5.7.0")
    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.hamcrest:hamcrest-library:2.2")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test:rules:1.5.0")
    testImplementation("androidx.test:runner:1.5.2")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("org.json:json:20231013")
    testImplementation("org.powermock:powermock-module-junit4:2.0.9")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.9")


    // Instrumented Testing
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("org.mockito:mockito-android:5.7.0")
}

// Task to run all tests
tasks.register("runAllTests") {
    dependsOn("testDebugUnitTest", "connectedAndroidTest")
}

// Test reporting configuration
tasks.withType<Test>().configureEach {
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
        showStackTraces = true
        showCauses = true
    }
}

// Jacoco configuration
configure<JacocoPluginExtension> {
    toolVersion = "0.8.8"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )

    val debugTree = fileTree("${buildDir}/intermediates/javac/debug/classes") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(buildDir) {
        include(
            "jacoco/testDebugUnitTest.exec",
            "outputs/code_coverage/debugAndroidTest/connected/**/*.ec"
        )
    })
}
