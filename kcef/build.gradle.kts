import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.vanniktech.maven.publish")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka")
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
    implementation(kotlin("stdlib"))
}

mavenPublishing {
    publishToMavenCentral(host = SonatypeHost.S01, automaticRelease = true)
    signAllPublications()
    coordinates("dev.datlag", "kcef", "1.0.0")

    pom {
        name.set(project.name)
        description.set("Kotlin implementation of jcefmaven with more modern setup and depending on JetBrains/jcef")
        url.set("https://github.com/DATL4G/KCEF")
        inceptionYear.set("2023")

        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        scm {
            url.set("https://github.com/DATL4G/KCEF")
            connection.set("scm:git:git://github.com/DATL4G/KCEF.git")
            developerConnection.set("scm:git:git://github.com/DATL4G/KCEF.git")
        }

        developers {
            developer {
                id.set("DATL4G")
                name.set("Jeff Retz")
                url.set("https://github.com/DatL4g")
            }
        }
    }
}