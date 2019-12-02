package com.malvinstn.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

fun Project.addsRxDependencies(configurationName: String = "implementation") {
    dependencies {
        add(configurationName, "io.reactivex.rxjava2:rxjava:2.2.14")
        add(configurationName, "io.reactivex.rxjava2:rxandroid:2.1.1")
        add(configurationName, "io.reactivex.rxjava2:rxkotlin:2.4.0")
    }
}
