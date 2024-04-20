import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21" apply false
    kotlin("plugin.serialization") version "1.9.21" apply false
    id("org.jetbrains.compose") version "1.5.11" apply false
    id("com.vanniktech.maven.publish") version "0.28.0" apply false
    id("org.jetbrains.dokka") version "1.9.20"
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://jogamp.org/deployment/maven") }
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://jogamp.org/deployment/maven") }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
    }
    tasks.withType<JavaCompile> {
        targetCompatibility = "17"
        options.compilerArgs.addAll(arrayOf(
            "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED"
        ))
        options.compilerArgs.addAll(arrayOf(
            "--add-exports", "java.desktop/java.awt.peer=ALL-UNNAMED"
        ))
        options.compilerArgs.addAll(arrayOf(
            "--add-exports", "java.desktop/sun.lwawt=ALL-UNNAMED"
        ))
        options.compilerArgs.addAll(arrayOf(
            "--add-exports", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
        ))
    }
}
