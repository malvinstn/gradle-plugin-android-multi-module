import com.malvinstn.gradle.addsRxDependencies

plugins {
    id("com.android.application")
    id("my-plugin")
}

android {
    buildTypes {
        getByName("debug") {
            isTestCoverageEnabled = true
        }
    }
}

addsRxDependencies()

dependencies {
    implementation(project(":lib1"))
    implementation(project(":lib2"))
    implementation("androidx.appcompat:appcompat:1.1.0")
}
