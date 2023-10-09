plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation("org.jogamp.gluegen:gluegen-rt:2.5.0")
    implementation("org.jogamp.jogl:jogl-all:2.5.0")
    implementation("net.java.dev.jna:jna-platform:5.13.0")
}