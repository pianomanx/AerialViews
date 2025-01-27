import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jmailen.kotlinter")
}

android {
    compileSdk =31

    defaultConfig {
        applicationId = "com.neilturner.aerialviews"
        minSdk = 23
        targetSdk = 31
        versionCode = 2 // Will be incremented automatically on release
        versionName = "1.1"

        manifestPlaceholders["analyticsCollectionEnabled"] = false
        manifestPlaceholders["crashlyticsCollectionEnabled"] = false
        manifestPlaceholders["performanceCollectionEnabled"] = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        dataBinding = true
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            //isMinifyEnabled = true
            //proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            isMinifyEnabled = true

            manifestPlaceholders["analyticsCollectionEnabled"] = true
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
            manifestPlaceholders["performanceCollectionEnabled"] = true
            //isDebuggable = true
        }
    }

    flavorDimensions.add("version")
    productFlavors {
        create("github") {
            dimension = "version"
        }
        create("beta") {
            dimension = "version"
            versionNameSuffix = "-beta2"
        }
        create("googleplay") {
            dimension = "version"
        }
    }
}

// Use different keys for different flavors of app
androidComponents {
    onVariants { variant ->
        if (variant.buildType == "release" && variant.flavorName.isNullOrEmpty()) {
            val keystoreFile = rootProject.file("signing/${variant.flavorName}.properties")
            if (keystoreFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystoreFile))
                val signingConfig = android.signingConfigs.create(variant.flavorName!!)
                signingConfig.storeFile = rootProject.file(keystoreProperties["storeFile"]!!)
                signingConfig.storePassword = keystoreProperties["storePassword"] as String?
                signingConfig.keyAlias = keystoreProperties["keyAlias"] as String?
                signingConfig.keyPassword = keystoreProperties["keyPassword"] as String?
                variant.signingConfig.setConfig(signingConfig)
            }
        }
    }
}

dependencies {
    // Kotlin
    val kotlinVersion = "1.6.10"
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    val coroutinesVersion = "1.6.0"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // Modern Storage
    implementation("com.google.modernstorage:modernstorage-permissions:1.0.0-alpha06")
    implementation("com.google.modernstorage:modernstorage-storage:1.0.0-alpha06")
    implementation("com.squareup.okio:okio:3.0.0")

    // Android X
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("androidx.leanback:leanback-preference:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:29.0.0"))
    implementation("com.google.firebase:firebase-analytics-ktx:20.1.0")
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.2.8")
    implementation("com.google.firebase:firebase-perf-ktx:20.0.5")

    // GSON
    implementation("com.google.code.gson:gson:2.9.0")

    // ExoPlayer
    implementation("com.google.android.exoplayer:exoplayer-core:2.16.1")

    // Kotpref
    implementation("com.chibatching.kotpref:kotpref:2.13.2")
    implementation("com.chibatching.kotpref:initializer:2.13.2")
    implementation("com.chibatching.kotpref:enum-support:2.13.2")

    // SMB
    implementation("com.hierynomus:smbj:0.11.3")

    //testImplementation("junit:junit:4.13.")
}