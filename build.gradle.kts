import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // literal versions required by the plugins DSL
    kotlin("jvm") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    // Ensure Kotlin and Java plugins are applied so java { ... } is available
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java")

    // Configure Kotlin's JVM extension (typed API)
    extensions.configure(KotlinJvmProjectExtension::class.java) {
        // jvm toolchain for Kotlin compilation
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }

        // Use the modern compilerOptions DSL (typed values)
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget("21"))
            jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
            freeCompilerArgs.addAll(listOf("-Xcontext-receivers"))
        }
    }

    // Configure Java toolchain for Java compilation tasks (keeps Kotlin & Java aligned)
    extensions.configure(JavaPluginExtension::class.java) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
}
