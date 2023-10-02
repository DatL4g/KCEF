import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0" apply false
    kotlin("plugin.serialization") version "1.9.0" apply false
    id("org.jetbrains.compose") version "1.5.2" apply false
    id("com.vanniktech.maven.publish") version "0.25.3" apply false
    id("org.jetbrains.dokka") version "1.9.0"
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
    }
    tasks.withType<JavaCompile> {
        targetCompatibility = "17"
        options.compilerArgs.addAll(arrayOf(
            "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED"
        ))
    }
}
