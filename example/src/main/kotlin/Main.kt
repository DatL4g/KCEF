import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.window.singleWindowApplication
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cef.browser.CefRendering

fun main() = singleWindowApplication {
    var initialized by remember { mutableStateOf(false) }
    var download by remember { mutableStateOf(-1) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            KCEF.init(
                builder = {
                    addArgs("--no-sandbox")
                    progress {
                        onInitialized {
                            initialized = true
                        }
                        onDownloading {
                            download = it.toInt()
                        }
                    }
                    settings {
                        noSandbox = true
                    }
                    release(true)
                }
            )
        }
    }

    if (initialized) {
        // CEF is definitely initialized here, so we can use the blocking method without produceState
        val client = KCEF.newClientBlocking()
        val browser = client.createBrowser(
            "https://github.com/DATL4G/KCEF",
            CefRendering.DEFAULT,
            false
        )
        val html = """
            <html>
                <head>
                    <title>KCEF</title>
                </head>
                <body>
                    Welcome to KCEF!
                </body>
            </html>
        """.trimIndent()

        Column(modifier = Modifier.fillMaxSize()) {
            Button(
                onClick = {
                    browser.loadHtml(html)
                }
            ) {
                Text(text = "Load HTML")
            }
            SwingPanel(
                factory = {
                    browser.uiComponent
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        if (download > -1) {
            Text("Downloading: $download%")
        } else {
            Text("Initializing please wait...")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            KCEF.disposeBlocking()
        }
    }
}