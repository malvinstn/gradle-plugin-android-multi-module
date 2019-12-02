package com.malvinstn.gradle.plugins

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class MyModulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Apply Required Plugins.
        project.plugins.apply("kotlin-android")
        project.plugins.apply("kotlin-android-extensions")

        project.extensions.create<MyModuleExtension>("myOptions")

        // Configure common android build parameters.
        val androidExtension = project.extensions.getByName("android")
        if (androidExtension is BaseExtension) {
            androidExtension.apply {
                compileSdkVersion(29)
                defaultConfig {
                    targetSdkVersion(29)
                    minSdkVersion(23)

                    versionCode = 1
                    versionName = "1.0.0"

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                // Configure common proguard file settings.
                val proguardFile = "proguard-rules.pro"
                when (this) {
                    is LibraryExtension -> defaultConfig {
                        consumerProguardFiles(proguardFile)
                    }
                    is AppExtension -> buildTypes {
                        getByName("release") {
                            isMinifyEnabled = true
                            isShrinkResources = true
                            proguardFiles(
                                getDefaultProguardFile("proguard-android-optimize.txt"),
                                proguardFile
                            )
                        }
                    }
                }

                // Java 8
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }
                project.tasks.withType(KotlinCompile::class.java).configureEach {
                    kotlinOptions {
                        jvmTarget = "1.8"
                    }
                }
            }
        }

        // Adds required dependencies for all modules.
        project.dependencies {
            add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.50")
            add("implementation", "androidx.core:core-ktx:1.0.2")

            add("testImplementation", "junit:junit:4.12")
            add("androidTestImplementation", "androidx.test.ext:junit:1.1.1")
            add("androidTestImplementation", "androidx.test.espresso:espresso-core:3.2.0")
        }

        // Read MyModuleExtension values in afterEvaluate block.
        project.afterEvaluate {
            project.extensions.getByType(MyModuleExtension::class.java).run {
                val jacocoOptions = this.jacoco
                if (jacocoOptions.isEnabled) {
                    // Setup jacoco tasks to generate coverage report for this module.
                    project.plugins.apply(JacocoPlugin::class.java)
                    project.plugins.all {
                        when (this) {
                            is LibraryPlugin -> {
                                project.extensions.getByType(LibraryExtension::class.java).run {
                                    configureJacoco(project, libraryVariants, jacocoOptions)
                                }
                            }
                            is AppPlugin -> {
                                project.extensions.getByType(AppExtension::class.java).run {
                                    configureJacoco(project, applicationVariants, jacocoOptions)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun configureJacoco(
        project: Project,
        variants: DomainObjectSet<out BaseVariant>,
        options: JacocoOptions
    ) {
        variants.all {
            val variantName = name
            val isDebuggable = this.buildType.isDebuggable
            if (!isDebuggable) {
                project.logger.info("Skipping Jacoco for $name because it is not debuggable.")
                return@all
            }

            project.tasks.register<JacocoReport>("jacoco${variantName.capitalize()}Report") {
                dependsOn(project.tasks["test${variantName.capitalize()}UnitTest"])
                val coverageSourceDirs = "src/main/java"

                val javaClasses = project
                    .fileTree("${project.buildDir}/intermediates/javac/$variantName") {
                        setExcludes(options.excludes)
                    }

                val kotlinClasses = project
                    .fileTree("${project.buildDir}/tmp/kotlin-classes/$variantName") {
                        setExcludes(options.excludes)
                    }

                // Using the default Jacoco exec file output path.
                val execFile = "jacoco/test${variantName.capitalize()}UnitTest.exec"

                executionData.setFrom(
                    project.fileTree("${project.buildDir}") {
                        setIncludes(listOf(execFile))
                    }
                )

                // Do not run task if there's no execution data.
                setOnlyIf { executionData.files.any { it.exists() } }

                classDirectories.setFrom(javaClasses, kotlinClasses)
                sourceDirectories.setFrom(coverageSourceDirs)
                additionalSourceDirs.setFrom(coverageSourceDirs)

                reports.xml.isEnabled = true
                reports.html.isEnabled = true
            }
        }
    }
}
