plugins {
    id("com.android.library")
    kotlin("android")
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
    listOf(
        "libs.kotlinStdlib",
        "libs.appCompat",
        "libs.design",
        "libs.support",
        "libs.preference",
        "libs.ktxCore",
        "libs.rxJava",
        "libs.rxAndroid",
        "libs.rxKotlin",
        "libs.roomRx",
        "libs.jodaTime",
        "libs.gson",
        "libs.okhttp",
        "libs.toasty",
        "libs.materialTypeface",
        "libs.materialDialogsCore",
        "libs.materialDialogsInput",
        "libs.iconicsCore",
        "libs.iconicsTypeface",
        "libs.textDrawable",
    ).forEach { path ->
        implementation(rootProject.extra(path))
    }
}
