plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

val ktorVersion = "2.3.4"

dependencies {
    api(project(":jcef"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.apache.commons:commons-compress:1.24.0")
}