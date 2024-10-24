import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
    id("maven-publish")
    id("signing")
}

dependencies {
    implementation("org.jogamp.gluegen:gluegen-rt:2.5.0")
    implementation("org.jogamp.jogl:jogl-all:2.5.0")

    implementation(files("src/main/third_party/thrift/libthrift-0.19.0.jar"))
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

mavenPublishing {
    publishToMavenCentral(host = SonatypeHost.S01, automaticRelease = true)
    signAllPublications()
    coordinates("dev.datlag", "jcef", "2024.04.20.4")

    pom {
        name.set(project.name)
        description.set("A simple framework for embedding Chromium-based browsers into Java-based applications.")
        url.set("https://github.com/DATL4G/jcef")
        inceptionYear.set("2023")

        licenses {
            license {
                url.set("https://github.com/DATL4G/jcef/blob/dev/LICENSE.txt")
            }
        }

        scm {
            url.set("https://github.com/DATL4G/jcef")
            connection.set("scm:git:git://github.com/DATL4G/jcef.git")
            developerConnection.set("scm:git:git://github.com/DATL4G/jcef.git")
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
