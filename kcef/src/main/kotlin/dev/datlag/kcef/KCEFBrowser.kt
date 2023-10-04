package dev.datlag.kcef

import kotlinx.coroutines.runBlocking
import org.cef.browser.CefBrowser
import org.cef.callback.CefNative

/**
 * Class that inherits the default [CefBrowser] with additional methods.
 */
class KCEFBrowser internal constructor(
    val client: KCEFClient,
    private val browser: CefBrowser
) : CefBrowser by browser {

    /**
     * Loads HTML content.
     *
     * Registers a virtual resource containing [html] that will be
     * available in the browser at [url] and loads [url].
     *
     * @param html content to load
     * @param url  the URL
     */
    @JvmOverloads
    fun loadHtml(html: String, url: String = BLANK_URI) {
        loadURL(KCEFFileSchemeHandlerFactory.registerLoadHtmlRequest(browser, html, url))
    }

    suspend fun evaluateJavaScript(
        javaScriptExpression: String
    ): String? = KCEFBrowserJsCall(javaScriptExpression, this).await()

    fun evaluateJavaScriptBlocking(
        javaScriptExpression: String
    ) = runBlocking {
        evaluateJavaScript(javaScriptExpression)
    }

    /**
     * Dispose this browser instance.
     */
    fun dispose() {
        browser.stopLoad()
        browser.setCloseAllowed()
        browser.close(true)
    }

    internal fun isCreated(): Boolean {
        return (browser as? CefNative)?.getNativeRef("CefBrowser")?.let { it != 0L } ?: false
    }

    companion object {
        const val BLANK_URI = "about:blank"
    }
}