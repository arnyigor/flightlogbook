plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdkVersion(29)
    buildToolsVersion("30.0.2")
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
//        forTest {
//        }
//        prerelease {
//        }
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        val srcDirs = getByName("androidTest").assets.srcDirs
        getByName("androidTest").assets.srcDirs(srcDirs + files("$projectDir/schemas"))
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":customfields"))

    listOf(
        "libs.kotlinStdlib",
        "libs.multidex",
        "libs.junit",
        "libs.dagger",
        "libs.rxJava",
        "libs.rxAndroid",
        "libs.rxKotlin",
        "libs.roomRuntime",
        "libs.roomRx",
        "libs.roomTesting",
        "libs.poi",
        "libs.stetho",
        "libs.okhttp",
        "libs.dropboxCoreSdk",
    ).forEach { path ->
//        implementation(rootProject.extra())
    }
    implementation(rootProject.extra("libs.kotlinStdlib"))
    implementation(rootProject.extra("libs.multidex"))
    kapt(rootProject.extra("libs.roomCompiler"))
    kapt(rootProject.extra("libs.daggerCompiler"))
    kapt(rootProject.extra("libs.moxyCompiler"))
    androidTestImplementation(rootProject.extra("libs.extJunit"))
    androidTestImplementation(rootProject.extra("libs.rules"))
    androidTestImplementation(rootProject.extra("libs.runner"))
}
