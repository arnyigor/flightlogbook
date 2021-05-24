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

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(project(":adapters"))
    implementation(project(":core"))

    listOf(
        "libs.kotlinStdlib",
        "libs.ktxCore",
        "libs.appCompat",
        "libs.design",
        "libs.recyclerView",
        "libs.cardView",
        "libs.constraint",
        "libs.moxy",
        "libs.moxyAndroidX",
        "libs.rxJava",
        "libs.rxAndroid",
        "libs.rxKotlin",
        "libs.junit",
        "libs.mockitoCore",
        "libs.dagger",
    ).forEach { path ->
        implementation(rootProject.extra(path))
    }
    kapt(rootProject.extra("libs.moxyCompiler"))
    kapt(rootProject.extra("libs.daggerCompiler"))
    androidTestImplementation(rootProject.extra("libs.extJunit"))
    androidTestImplementation(rootProject.extra("libs.rules"))
    androidTestImplementation(rootProject.extra("libs.mockitoAndroid"))
}