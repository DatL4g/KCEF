# KCEF

**Kotlin implementation of jcefmaven with more modern setup and depending on JetBrains/jcef**

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

This library is published to [Maven Central](https://mvnrepository.com/artifact/dev.datlag.kcef/kcef).

```gradle
repositories {
    mavenCentral()
}
```

### Version

The version depends on [JetBrains/JetBrainsRuntime](https://github.com/JetBrains/JetBrainsRuntime/releases) and [JetBrains/jcef](https://github.com/JetBrains/jcef).

Take a look at the [releases](https://github.com/DATL4G/KCEF/releases) for more details.
Newer version may or may not be working.

#### Kotlin DSL

```kotlin
dependencies {
    implementation("dev.datlag:kcef:$version")
}
```

#### Version catalog

```toml
[versions]
kcef = "" # put your wanted version here

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
