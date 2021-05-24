import org.jetbrains.kotlin.konan.properties.Properties
apply(from = "dep_aliases")
apply(from = "commonconfig")
plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.firebase.crashlytics")
}

android {
    signingConfigs {
        named("release")
    }

    defaultConfig {
        applicationId = "com.arny.flightlogbook"
        setProperty("archivesBaseName", "$applicationId-v($versionName)")
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
                arg("dagger.gradle.incremental", "true")
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        named("debug").configure {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            buildConfigField("String", "app_name", "Pilot Logbook[DEBUG]")
            isMinifyEnabled = false
            isUseProguard = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            testProguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        named("release").configure {
            isDebuggable = false
            buildConfigField("String", "app_name", "Pilot Logbook")
            isMinifyEnabled = true
            isUseProguard = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            testProguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        named("prerelease").configure {
            isDebuggable = true
            buildConfigField("String", "app_name", "Pilot Logbook[PRERLS]")
            isMinifyEnabled = true
            isUseProguard = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            testProguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    packagingOptions {
        exclude("META-INF/ASL2.0")
        exclude("META-INF/LICENSE")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/MANIFEST.MF")
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
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))
    implementation(project(":adapters"))
    implementation(project(":domain"))
    implementation(project(":customfields"))
    implementation(project(":data"))

    listOf(
        "libs.kotlinStdlib",
        "libs.multidex",
        "libs.firebaseCrash",
        "libs.ktxCore",
        "libs.lifecycleExt",
        "libs.junit",
        "libs.mockitoCore",
        "libs.appCompat",
        "libs.design",
        "libs.recyclerView",
        "libs.preference",
        "libs.drawerlayout",
        "libs.cardView",
        "libs.constraint",
        "libs.vector",
        "libs.rxJava",
        "libs.rxAndroid",
        "libs.rxKotlin",
        "libs.roomRuntime",
        "libs.roomRx",
        "libs.moxy",
        "libs.moxyAndroidX",
        "libs.moxyKtx",
        "libs.dagger",
        "libs.rxpermissions",
        "libs.betterpickers",
        "libs.toasty",
        "libs.dropboxCoreSdk",
        "libs.jodaTime",
        "libs.materialTypeface",
        "libs.materialDialogsCore",
        "libs.materialDialogsInput",
        "libs.drawer",
        "libs.drawerNav",
        "libs.drawerIcon",
        "libs.stetho",
        "libs.inputmask",
    ).forEach { path ->
        implementation(rootProject.extra(path))
    }
    kapt(rootProject.extra("libs.roomCompiler"))
    kapt(rootProject.extra("libs.daggerCompiler"))
    kapt(rootProject.extra("libs.moxyCompiler"))
    androidTestImplementation(rootProject.extra("libs.mockitoAndroid"))
    androidTestImplementation(rootProject.extra("libs.assertjCore"))
    androidTestImplementation(rootProject.extra("libs.assertjAndroid"))
    androidTestImplementation(rootProject.extra("libs.extJunit"))
    androidTestImplementation(rootProject.extra("libs.rules"))
    androidTestImplementation(rootProject.extra("libs.runner"))
    debugImplementation(rootProject.extra("libs.dbDebug"))
}

android.applicationVariants.forEach { variant ->
    variant.outputs
        .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
        .forEach { output ->
            output.outputFileName = output.outputFileName
                .replace("-release", "-release-FLB-${variant.versionName}-${variant.name}.apk")
                .replace("-debug", "-debug-FLB-${variant.versionName}-${variant.name}.apk")
        }
}

val props = Properties()
val propFile = File(System.getProperty("user.dir") + "/signing.properties")
if (propFile.canRead()) {
    propFile.inputStream().use {
        props.load(it)
    }
    if (props.containsKey("STORE_FILE") && props.containsKey("STORE_PASSWORD") &&
        props.containsKey("KEY_ALIAS") && props.containsKey("KEY_PASSWORD")
    ) {
        android.signingConfigs.getByName("release").storeFile(file(props["STORE_FILE"] ?: ""))
        android.signingConfigs.getByName("release")
            .storePassword(props["STORE_PASSWORD"].toString())
        android.signingConfigs.getByName("release").keyAlias(props["KEY_ALIAS"].toString())
        android.signingConfigs.getByName("release").keyPassword(props["KEY_PASSWORD"].toString())
    } else {
        println("signing.properties found but some entries are missing")
    }
}else{
    println("signing.properties not found")
}

