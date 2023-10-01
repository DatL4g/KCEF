import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.window.singleWindowApplication
import dev.datlag.kcef.kcef
import org.cef.browser.CefRendering

fun main() = singleWindowApplication {
    var initialized by remember { mutableStateOf(false) }

    val cefApp = kcef {
        progress {
            onInitialized {
                initialized = true
            }
        }
    }.build()

    if (initialized) {
        val client = cefApp.createClient()
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
        Text("Initializing please wait...")
    }
}