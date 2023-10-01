plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":kcef"))
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
