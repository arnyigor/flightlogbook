import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.arny.flightlogbook"
    compileSdk = 34
    val vMajor = 1
    val vMinor = 0
    val vBuild = 0

    defaultConfig {
        applicationId = "com.arny.flightlogbook"
        minSdk = 21
        targetSdk = 34
        versionCode = vMajor * 1000 + vMinor * 100 + vBuild
        versionName = "$vMajor" + ".${vMinor}" + ".${vBuild}"
        setProperty("archivesBaseName", "$applicationId-v($versionName)-c($versionCode)")
        vectorDrawables.useSupportLibrary = true
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val properties = Properties().apply {
                load(File("signing.properties").reader())
            }
            storeFile = File(properties.getProperty("STORE_FILE"))
            storePassword = properties.getProperty("STORE_PASSWORD")
            keyPassword = properties.getProperty("KEY_PASSWORD")
            keyAlias = properties.getProperty("KEY_ALIAS")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
        release {
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.moxy)
    kapt(libs.moxy.compiler)
    implementation(libs.moxy.android)
    implementation(libs.moxy.ktx)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.viewmodel.extensions)
    implementation(libs.androidx.viewmodelKtx)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activityKtx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.fragmentKtx)

    implementation(libs.androidx.room)
    implementation(libs.androidx.roomktx)
    kapt(libs.androidx.compiler)

    implementation(libs.okhttp3)
    implementation(libs.coroutins)
    implementation(libs.coroutinsCore)

    implementation(libs.rxjava)
    implementation(libs.rxjava.rxAndroid)
    implementation(libs.rxjava.rxKotlin)

    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.fragmentKtx)
    implementation(libs.androidx.navigation.uiKtx)

    implementation(libs.dagger)
    implementation(libs.dagger.android)
    implementation(libs.dagger.android.support)
    kapt(libs.dagger.compiler)
    kapt(libs.dagger.android.processor)
    implementation(libs.jodatime)
    implementation(libs.materialDialogs.core)
    implementation(libs.materialDialogs.input)
    implementation(libs.materialDialogs.color)
    implementation(libs.timber)
    implementation(libs.gson)
    implementation(libs.toasty)
    implementation(libs.mikepenz.materialdrawer)
    implementation(libs.poi)
    implementation(libs.input.mask)
    implementation(libs.betterpickers)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}