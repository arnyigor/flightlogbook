plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}
android {
    buildTypes {
        named("debug").configure {
            isDebuggable = true
        }
        named("debug").configure {
            isDebuggable = false
        }
        named("forTest").configure {
            isDebuggable = false
        }
        named("prerelease").configure {
            isDebuggable = true
        }
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":customfields"))

    listOf(
        "libs.kotlinStdlib",
        "libs.multidex",
        "libs.junit",
        "libs.assertjCore",
        "libs.mockitoCore",
        "libs.dagger",
        "libs.poi",
        "libs.okhttp",
        "libs.dropboxCoreSdk",
    ).forEach { path ->
        implementation(rootProject.extra(path))
    }
    kapt(rootProject.extra("libs.daggerCompiler"))
    kapt(rootProject.extra("libs.moxyCompiler"))
    androidTestImplementation(rootProject.extra("libs.extJunit"))
    androidTestImplementation(rootProject.extra("libs.rules"))
    androidTestImplementation(rootProject.extra("libs.assertjAndroid"))
    androidTestImplementation(rootProject.extra("libs.mockitoAndroid"))
}
