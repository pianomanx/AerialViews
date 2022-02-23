val kotlinVersion: String by project

buildscript {
    val kotlinVersion = "1.6.10"

    repositories {
        mavenCentral()
        google()
        maven("https://plugins.gradle.org/m2/")
    }
    
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.1")
        classpath("com.google.firebase:perf-plugin:1.4.1")
        classpath("org.jmailen.gradle:kotlinter-gradle:3.8.0")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}