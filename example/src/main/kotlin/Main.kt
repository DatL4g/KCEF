import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.window.singleWindowApplication
import dev.datlag.kcef.KCEF
import dev.datlag.kcef.KCEFCookieManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                },
                onError = {
                    it?.printStackTrace()
                }
            )
        }
    }

    if (initialized) {
        // CEF is definitely initialized here, so we can use the blocking method without produceState
        val client = remember { KCEF.newClientBlocking() }
        val browser = remember {
            client.createBrowser(
                "https://github.com/DATL4G/KCEF",
                CefRendering.DEFAULT,
                false
            )
        }
        val html = """
            <html>
                <head>
                    <title>KCEF</title>
                </head>
                <body>
                    <script type="text/javascript">
                        function callJS() {
                            return 'Response from JS';
                        }
                    </script>
                    <h1>Welcome to KCEF!</h1>
                    <h2 id="subtitle">Basic Html Test</h2>
                </body>
            </html>
        """.trimIndent()

        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                var evaluateActive by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        browser.loadHtml(html)
                        evaluateActive = true
                    }
                ) {
                    Text(text = "Load HTML")
                }
                Button(
                    onClick = {
                        browser.evaluateJavaScript("""
                            document.getElementById("subtitle").innerText = "Hello from Kotlin";
                            return callJS();
                        """.trimIndent()) {
                            println("JS Result: $it")
                        }
                    },
                    enabled = evaluateActive
                ) {
                    Text(text = "Evaluate JavaScript to Java Console")
                }
                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            KCEFCookieManager.instance.getCookiesWhile().map { "Cookie Name: ${it.name}" }.forEach(::println)
                        }
                    }
                ) {
                    Text(text = "List Cookies")
                }
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