import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    compileSdk =31

    defaultConfig {
        applicationId = "com.neilturner.aerialviews"
        minSdk =23
        targetSdk =30
        versionCode = 1 // Will be incremented automatically on release
        versionName = "1.0"

        resValue("string", "app_name", "@string/app_name_release")

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

            // Use different keys for different flavors of app
            productFlavors.forEach { flavor ->
                val keystoreFile = rootProject.file("signing/${flavor.name}.properties")
                if (keystoreFile.exists()) {
                    val keystoreProperties = Properties()
                    keystoreProperties.load(FileInputStream(keystoreFile))
                    flavor.signingConfig = android.signingConfigs.create(flavor.name)
                    flavor.signingConfig?.storeFile = rootProject.file(keystoreProperties["storeFile"]!!)
                    flavor.signingConfig?.storePassword = keystoreProperties["storePassword"] as String?
                    flavor.signingConfig?.keyAlias = keystoreProperties["keyAlias"] as String?
                    flavor.signingConfig?.keyPassword = keystoreProperties["keyPassword"] as String?
                }
            }

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
            applicationIdSuffix = ".beta"
            versionNameSuffix = "-beta"
        }
        create("googleplay") {
            dimension = "version"
        }
    }
}

dependencies {
    // Kotlin
    val kotlinVersion = project.properties["kotlinVersion"]
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    val coroutinesVersion = "1.6.0"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // Android X
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("androidx.leanback:leanback-preference:1.0.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.activity:activity-ktx:1.4.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:29.0.0"))
    implementation("com.google.firebase:firebase-analytics-ktx:20.0.2")
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.2.6")
    implementation("com.google.firebase:firebase-perf-ktx:20.0.4")

    // GSON
    implementation("com.google.code.gson:gson:2.8.9")

    // ExoPlayer
    implementation("com.google.android.exoplayer:exoplayer-core:2.15.1")

    // Kotpref
    implementation("com.chibatching.kotpref:kotpref:2.13.1")
    implementation("com.chibatching.kotpref:initializer:2.13.1")
    implementation("com.chibatching.kotpref:enum-support:2.13.1")

    // SMB
    implementation("com.hierynomus:smbj:0.11.3")

    //testImplementation("junit:junit:4.13.")
}