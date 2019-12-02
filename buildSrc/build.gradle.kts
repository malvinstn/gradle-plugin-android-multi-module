plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("my-plugin") {
            id = "my-plugin"
            implementationClass = "com.malvinstn.gradle.plugins.MyModulePlugin"
        }
    }
}

repositories {
    google()
    mavenCentral()
    jcenter()
}

dependencies {
    compileOnly(gradleApi())

    implementation("com.android.tools.build:gradle:3.6.0-beta04")
    implementation(kotlin("gradle-plugin", "1.3.50"))
    implementation(kotlin("android-extensions"))

    implementation("org.jacoco:org.jacoco.core:0.8.4")
}
