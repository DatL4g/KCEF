plugins {
    kotlin("jvm")
}

val ktorVersion = "2.3.4"

dependencies {
    api(project(":jcef"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
}