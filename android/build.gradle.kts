plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("com.github.jakepurple13.HelpfulTools:helpfulutils:10.6.5")
    val datastore = "1.1.0-alpha04"
    implementation("androidx.datastore:datastore:$datastore")
    implementation("androidx.datastore:datastore-preferences:$datastore")
}

android {
    compileSdkVersion(33)
    compileSdk = 33
    defaultConfig {
        applicationId = "com.example.android"
        minSdkVersion(24)
        minSdk = 24
        targetSdk = 33
        targetSdkVersion(33)
        versionCode = 1
        versionName = "1.0-SNAPSHOT"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

tasks.register("BuildAndRun") {
    doFirst {
        exec {
            workingDir(projectDir.parentFile)
            commandLine("./gradlew", "android:build")
            commandLine("./gradlew", "android:installDebug")
        }
    }
}