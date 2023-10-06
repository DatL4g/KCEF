package dev.datlag.kcef

import kotlinx.coroutines.runBlocking
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Class that inherits the default [CefBrowser] with additional methods.
 */
class KCEFBrowser internal constructor(
    val client: KCEFClient,
    private val browser: CefBrowser
) : CefBrowser by browser {

    val mainKCEFFrame: KCEFFrame
        get() = getMainFrame() as KCEFFrame

    val focusedKCEFFrame: KCEFFrame
        get() = getFocusedFrame() as KCEFFrame

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

    /**
     * Execute the Javascript code and get the response in [callback].
     *
     * @param javaScriptExpression the passed JavaScript code should be either:
     * * a valid single-line JavaScript expression
     * * a valid multi-line function-body with at least one "return" statement
     * @param callback a [KCEFFrame.EvaluateJavascriptCallback] listener to handle the response
     */
    fun evaluateJavaScript(
        javaScriptExpression: String,
        callback: KCEFFrame.EvaluateJavascriptCallback
    ) {
        val functionName = client.evaluateJSHandler.queryFunction(client)
        client.evaluateJSHandler.addHandler(functionName) { response ->
            callback(response)
            null
        }

        val executeJs = KCEFJSHandler.wrapWithErrorHandling(javaScriptExpression, functionName)
        executeJavaScript(executeJs, "", 0)
    }

    /**
     * Execute the Javascript code and wait for a response.
     * This [suspend] equivalent is useful if you want to work with timeouts for example.
     *
     * @param javaScriptExpression the passed JavaScript code should be either:
     * * a valid single-line JavaScript expression
     * * a valid multi-line function-body with at least one "return" statement
     *
     * @see evaluateJavaScript
     * @return a nullable [String] which is the response
     */
    suspend fun evaluateJavaScript(javaScriptExpression: String): String? = suspendCoroutine { continuation ->
        evaluateJavaScript(javaScriptExpression) { response ->
            continuation.resume(response)
        }
    }

    /**
     * Blocking equivalent of [evaluateJavaScript]
     *
     * @see evaluateJavaScript
     */
    fun evaluateJavaScriptBlocking(javaScriptExpression: String): String? = runBlocking {
        evaluateJavaScript(javaScriptExpression)
    }

    override fun getFrame(identifier: Long): CefFrame? {
        return browser.getFrame(identifier)?.let { KCEFFrame(client, it) }
    }

    fun getKCEFFrame(identifier: Long): KCEFFrame? = getFrame(identifier) as KCEFFrame?

    override fun getFrame(name: String?): CefFrame? {
        return browser.getFrame(name)?.let { KCEFFrame(client, it) }
    }

    fun getKCEFFrame(name: String?): KCEFFrame? = getFrame(name) as KCEFFrame?

    override fun getMainFrame(): CefFrame {
        return KCEFFrame(client, browser.mainFrame)
    }

    override fun getFocusedFrame(): CefFrame {
        return KCEFFrame(client, browser.focusedFrame)
    }

    /**
     * Dispose this browser instance.
     */
    fun dispose() {
        browser.stopLoad()
        browser.setCloseAllowed()
        browser.close(true)
    }

    companion object {
        const val BLANK_URI = "about:blank"
    }
}