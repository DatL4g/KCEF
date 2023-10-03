package dev.datlag.kcef

import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefRendering
import org.cef.browser.CefRequestContext
import org.cef.handler.CefContextMenuHandler
import org.cef.handler.CefDialogHandler
import org.cef.handler.CefDisplayHandler
import org.cef.handler.CefDownloadHandler
import org.cef.handler.CefDragHandler
import org.cef.handler.CefFocusHandler
import org.cef.handler.CefJSDialogHandler
import org.cef.handler.CefKeyboardHandler
import org.cef.handler.CefLifeSpanHandler
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefPermissionHandler
import org.cef.handler.CefPrintHandler
import org.cef.handler.CefRenderHandler
import org.cef.handler.CefRequestHandler
import org.cef.handler.CefWindowHandler

/**
 * Class that can create a [KCEFBrowser] instance and inherits the default [CefClient] methods.
 */
class KCEFClient internal constructor(
    private val client: CefClient
) : CefContextMenuHandler by client,
    CefDialogHandler by client,
    CefDisplayHandler by client,
    CefDownloadHandler by client,
    CefDragHandler by client,
    CefFocusHandler by client,
    CefPermissionHandler by client,
    CefJSDialogHandler by client,
    CefKeyboardHandler by client,
    CefLifeSpanHandler by client,
    CefLoadHandler by client,
    CefPrintHandler by client,
    CefRenderHandler by client,
    CefRequestHandler by client,
    CefWindowHandler by client {

    /**
     * Create a [KCEFBrowser] instance
     *
     * @param browser the default [CefBrowser] to inherit from
     * @return [KCEFBrowser]
     */
    fun createBrowser(browser: CefBrowser): KCEFBrowser = KCEFBrowser(this, browser)

    /**
     * Create a [KCEFBrowser] instance
     *
     * @param url the default URL to launch with
     * @param rendering [CefRendering] option that specifies the CEF rendering
     * @param isTransparent whether the browser is transparent
     * @return [KCEFBrowser]
     */
    @JvmOverloads
    fun createBrowser(
        url: String?,
        rendering: CefRendering = CefRendering.DEFAULT,
        isTransparent: Boolean = false
    ) = createBrowser(client.createBrowser(url, rendering, isTransparent))

    /**
     * Create a [KCEFBrowser] instance
     *
     * @param url the default URL to launch with
     * @param rendering [CefRendering] option that specifies the CEF rendering
     * @param isTransparent whether the browser is transparent
     * @param context [CefRequestContext] handling the request
     * @return [KCEFBrowser]
     */
    @JvmOverloads
    fun createBrowser(
        url: String?,
        rendering: CefRendering = CefRendering.DEFAULT,
        isTransparent: Boolean = false,
        context: CefRequestContext
    ) = createBrowser(client.createBrowser(url, rendering, isTransparent, context))

    override fun onCursorChange(browser: CefBrowser?, cursorType: Int): Boolean {
        return client.onCursorChange(browser, cursorType)
    }

    companion object
}