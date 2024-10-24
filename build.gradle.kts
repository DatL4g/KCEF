import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.21" apply false
    kotlin("plugin.serialization") version "2.0.21" apply false
    id("org.jetbrains.compose") version "1.6.11" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.vanniktech.maven.publish") version "0.28.0" apply false
    id("org.jetbrains.dokka") version "1.9.20"
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://jogamp.org/deployment/maven")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://jogamp.org/deployment/maven")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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
