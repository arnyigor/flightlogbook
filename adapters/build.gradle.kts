apply {
    from("${rootProject.projectDir}/commonconfig.gradle")
    from("${rootProject.projectDir}/dependenciesAll.gradle")
}
plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    defaultConfig {
    }

    buildTypes {

    }

}

dependencies {
    val kotlinVersion = rootProject.extra("kotlinVersion")
    implementation("kotlin-stdlib:$kotlinVersion") {
        artifact(delegateClosureOf<DependencyArtifact> {
            name = "org.jetbrains.kotlin"
        })
    }



//    implementation(rootProject.extra("libs.design"))
//    implementation(rootProject.extra("libs.support"))
}
