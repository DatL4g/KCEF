# Using KCEF in Compose applications

This section provides information about using KCEF in compose desktop applications.

## Initialize

You have to initialize KCEF first, you should do this in an I/O thread, since it will download required packages on first run.
The `KCEF.init` method only needs to be called once, but it's thread-safe, that means it not a problem to call it multiple times.

> **Note**
> Make sure to exclude the `installDir` from upstreaming by adding it to the `.gitignore`.
> The downloaded files are platform-specific and therefore differ to each user.

```kotlin
var downloadProgress by remember { mutableStateOf(-1F) }
var initialized by remember { mutableStateOf(false) } // if true, KCEF can be used to create clients, browsers etc
val bundleLocation = System.getProperty("compose.application.resources.dir")?.let { File(it) } ?: File(".")

LaunchedEffect(Unit) {
  withContext(Dispatchers.IO) { // IO scope recommended but not required
    KCEF.init(
      builder = {
        installDir(File(bundleLocation, "kcef-bundle")) // recommended, but not necessary

        progress {
          onDownloading {
            downloadProgress = it
            // use this if you want to display a download progress for example
          }
          onInitialized {
            initialized = true
          }
        }
      },
      onError = {
        // error during initialization
      },
      onRestartRequired = {
        // all required CEF packages downloaded but the application needs a restart to load them (unlikely to happen)
      }
    )
  }
}
```

After initialization finished you can run your app as usual, use the onInitialized progress listener for this.

## Disposal

When the browser or client instance is no longer needed, dispose them to free up resources.

```kotlin
val client = KCef.newClient()

DisposableEffect(Unit) {
  onDispose {
    client.dispose()
  }
}
```

When no new browser or client instance is ever needed again during runtime, dispose the whole KCEF object.

```kotlin
DisposableEffect(Unit) {
  onDispose {
    KCEF.dispose()
  }
}
```

## Flags

As mentioned in the [README.md](README.md#flags), some platforms need flags to run properly.
Adding them is pretty easy, by just adding them to your compose configuration in your `build.gradle.kts`:

```kotlin
compose.desktop {
    application {
      jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
      jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED") // recommended but not necessary

      if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }
    }
}
```
