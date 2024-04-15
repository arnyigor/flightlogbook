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
    val vMajor = 6
    val vMinor = 4
    val vBuild = 2

    defaultConfig {
        applicationId = "com.arny.flightlogbook"
        minSdk = 21
        targetSdk = 34
        versionCode = vMajor * 100 + vMinor * 10 + vBuild
        versionName = "$vMajor" + ".${vMinor}" + ".${vBuild}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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