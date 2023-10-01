import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.window.singleWindowApplication
import dev.datlag.kcef.kcef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cef.CefApp
import org.cef.browser.CefRendering

fun main() = singleWindowApplication {
    var initialized by remember { mutableStateOf(false) }
    var download by remember { mutableStateOf(0F) }

    val cefApp by produceState<CefApp?>(null) {
        value = withContext(Dispatchers.IO) {
            kcef {
                addArgs("--no-sandbox")
                progress {
                    onInitialized {
                        initialized = true
                    }
                    onDownloading {
                        download = it
                    }
                }
                settings {
                    noSandbox = true
                }
                release(true)
            }.build()
        }
    }

    if (initialized) {
        val client = cefApp!!.createClient()
        val browser = client.createBrowser(
            "https://github.com/DATL4G/KCEF",
            CefRendering.DEFAULT,
            false
        )

        SwingPanel(
            factory = {
                browser.uiComponent
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        if (download > 0F) {
            Text("Downloading: $download%")
        } else {
            Text("Initializing please wait...")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cefApp?.dispose()
        }
    }
}