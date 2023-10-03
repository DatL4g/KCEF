package dev.datlag.kcef

import org.cef.browser.CefBrowser

/**
 * Class that inherits the default [CefBrowser] with additional methods.
 */
class KCEFBrowser internal constructor(
    private val client: KCEFClient,
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