import com.vanniktech.maven.publish.SonatypeHost
import java.net.URL

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.vanniktech.maven.publish")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka")
}

val ktorVersion = "2.3.10"

dependencies {
    api(project(":jcef"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation(kotlin("stdlib"))
}

tasks.dokkaHtmlPartial {
    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory.set(file("src"))
            remoteUrl.set(URL("https://github.com/DatL4g/KCEF/tree/master/kcef/src"))
        }
    }
}

mavenPublishing {
    publishToMavenCentral(host = SonatypeHost.S01, automaticRelease = true)
    signAllPublications()
    coordinates("dev.datlag", "kcef", "2024.04.20.2")

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
