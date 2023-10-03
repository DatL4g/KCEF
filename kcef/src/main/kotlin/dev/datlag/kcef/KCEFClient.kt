package dev.datlag.kcef

import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefRendering
import org.cef.browser.CefRequestContext

class KCEFClient(
    val client: CefClient
) {
    fun createBrowser(browser: CefBrowser): KCEFBrowser = KCEFBrowser(client, browser)

    @JvmOverloads
    fun createBrowser(
        url: String?,
        rendering: CefRendering = CefRendering.DEFAULT,
        isTransparent: Boolean = false
    ) = createBrowser(client.createBrowser(url, rendering, isTransparent))

    @JvmOverloads
    fun createBrowser(
        url: String?,
        rendering: CefRendering = CefRendering.DEFAULT,
        isTransparent: Boolean = false,
        context: CefRequestContext
    ) = createBrowser(client.createBrowser(url, rendering, isTransparent, context))
}