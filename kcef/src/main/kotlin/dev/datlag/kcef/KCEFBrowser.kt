package dev.datlag.kcef

import org.cef.CefClient
import org.cef.browser.CefBrowser

class KCEFBrowser(
    private val client: CefClient,
    private val browser: CefBrowser
) : CefBrowser by browser {

    @JvmOverloads
    fun loadHtml(html: String, url: String = BLANK_URI) {
        loadURL(KCEFFileSchemeHandlerFactory.registerLoadHtmlRequest(browser, html, url))
    }

    fun dispose() {
        browser.stopLoad()
        browser.setCloseAllowed()
        browser.close(true)
    }

    companion object {
        const val BLANK_URI = "about:blank"
    }
}