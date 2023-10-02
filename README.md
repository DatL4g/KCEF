# KCEF

**Kotlin equivalent of jcefmaven with a more modern setup and depending on JetBrains/jcef**

Visit the JCEF repo at [JetBrains/jcef](https://github.com/JetBrains/jcef) and the jcefmaven repo at [jcefmaven/jcefmaven](https://github.com/jcefmaven/jcefmaven/)

## Supports

Please take a look at [JetBrains/JetBrainsRuntime](https://github.com/JetBrains/JetBrainsRuntime/releases) for a full list of supported targets.

| OS | Arch |
|----|------|
|![Linux](https://cdn.simpleicons.org/linux/000/fff)    | amd64, aarch64 |
|![Windows](https://cdn.simpleicons.org/windows/000/fff)| amd64, aarch64 |
|![MacOS](https://cdn.simpleicons.org/macos/000/fff)    | amd64, aarch64 |

## Installation

### Repository

This library is published to [Maven Central](https://mvnrepository.com/artifact/dev.datlag/kcef).

```gradle
repositories {
    mavenCentral()
}
```

### Version

The version depends on [JetBrains/JetBrainsRuntime](https://github.com/JetBrains/JetBrainsRuntime/releases) and [JetBrains/jcef](https://github.com/JetBrains/jcef).

Take a look at the [releases](https://github.com/DATL4G/KCEF/releases) for more details.

#### Kotlin DSL

```kotlin
dependencies {
    implementation("dev.datlag:kcef:$version")
    
    // or with version catalog
    implementation(libs.kcef)
}
```

#### Version catalog

```toml
[versions]
kcef = "1.0.0" # put your wanted version here

[libraries]
kcef = { group = "dev.datlag", name = "kcef", version.ref = "kcef" }
```

## Usage

TBD

## Flags

Some platforms require the addition of specific flags. To use on MacOSX, add the following JVM flags:

```
--add-opens java.desktop/sun.awt=ALL-UNNAMED
--add-opens java.desktop/sun.lwawt=ALL-UNNAMED
--add-opens java.desktop/sun.lwawt.macosx=ALL-UNNAMED
```

For gradle project, you can configure it in the build.gradle.kts like that:

```kotlin
afterEvaluate {
    tasks.withType<JavaExec> {
        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }
    }
}
```

## Support the project

[![Github-sponsors](https://img.shields.io/badge/sponsor-30363D?style=for-the-badge&logo=GitHub-Sponsors&logoColor=#EA4AAA)](https://github.com/sponsors/DATL4G)
[![PayPal](https://img.shields.io/badge/PayPal-00457C?style=for-the-badge&logo=paypal&logoColor=white)](https://paypal.me/datlag)

### This is a non-profit project!

Sponsoring to this project means sponsoring to all my projects!
So the further text is not to be attributed to this project, but to all my apps and libraries.

Supporting this project helps to keep it up-to-date. You can donate if you want or contribute to the project as well.
This shows that the library is used by people, and it's worth to maintain.
