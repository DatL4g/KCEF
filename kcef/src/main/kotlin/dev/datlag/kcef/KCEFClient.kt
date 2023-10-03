package dev.datlag.kcef

import kotlinx.coroutines.Runnable
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefRendering
import org.cef.browser.CefRequestContext
import org.cef.handler.CefAppStateHandler
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
    CefWindowHandler by client,
    CefAppStateHandler by client {

    val info: String
        get() = client.info

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

    fun setOnDisposeCallback(onDisposed: Runnable) {
        client.setOnDisposeCallback(onDisposed)
    }

    fun addContextMenuHandler(handler: CefContextMenuHandler) = apply {
        client.addContextMenuHandler(handler)
    }

    fun removeContextMenuHandler() = apply {
        client.removeContextMenuHandler()
    }

    fun addDialogHandler(handler: CefDialogHandler) = apply {
        client.addDialogHandler(handler)
    }

    fun removeDialogHandler() = apply {
        client.removeDialogHandler()
    }

    fun addDisplayHandler(handler: CefDisplayHandler) = apply {
        client.addDisplayHandler(handler)
    }

    fun removeDisplayHandler() = apply {
        client.removeDisplayHandler()
    }

    fun addDownloadHandler(handler: CefDownloadHandler) = apply {
        client.addDownloadHandler(handler)
    }

    fun removeDownloadHandler() = apply {
        client.removeDownloadHandler()
    }

    fun addDragHandler(handler: CefDragHandler) = apply {
        client.addDragHandler(handler)
    }

    fun removeDragHandler() = apply {
        client.removeDragHandler()
    }

    fun addFocusHandler(handler: CefFocusHandler) = apply {
        client.addFocusHandler(handler)
    }

    fun removeFocusHandler() = apply {
        client.removeFocusHandler()
    }

    fun addPermissionHandler(handler: CefPermissionHandler) = apply {
        client.addPermissionHandler(handler)
    }

    fun removePermissionHandler() = apply {
        client.removePermissionHandler()
    }

    fun addJSDialogHandler(handler: CefJSDialogHandler) = apply {
        client.addJSDialogHandler(handler)
    }

    fun removeJSDialogHandler() = apply {
        client.removeJSDialogHandler()
    }

    fun addKeyboardHandler(handler: CefKeyboardHandler) = apply {
        client.addKeyboardHandler(handler)
    }

    fun removeKeyboardHandler() = apply {
        client.removeKeyboardHandler()
    }

    fun addLifeSpanHandler(handler: CefLifeSpanHandler) = apply {
        client.addLifeSpanHandler(handler)
    }

    fun removeLifeSpanHandler() = apply {
        client.removeLifeSpanHandler()
    }

    fun addLoadHandler(handler: CefLoadHandler) = apply {
        client.addLoadHandler(handler)
    }

    fun removeLoadHandler() = apply {
        client.removeLoadHandler()
    }

    fun addPrintHandler(handler: CefPrintHandler) = apply {
        client.addPrintHandler(handler)
    }

    fun removePrintHandler() = apply {
        client.removePrintHandler()
    }

    fun addRequestHandler(handler: CefRequestHandler) = apply {
        client.addRequestHandler(handler)
    }

    fun removeRequestHandler() = apply {
        client.removeRequestHandler()
    }

    fun getContextMenuHandler(): CefContextMenuHandler = this
    fun getDialogHandler(): CefDialogHandler = this
    fun getDisplayHandler(): CefDisplayHandler = this
    fun getDownloadHandler(): CefDownloadHandler = this
    fun getDragHandler(): CefDragHandler = this
    fun getFocusHandler(): CefFocusHandler = this
    fun getPermissionHandler(): CefPermissionHandler = this
    fun getJSDialogHandler(): CefJSDialogHandler = this
    fun getKeyboardHandler(): CefKeyboardHandler = this
    fun getLifeSpanHandler(): CefLifeSpanHandler = this
    fun getLoadHandler(): CefLoadHandler = this
    fun getPrintHandler(): CefPrintHandler = this
    fun getRenderHandler(): CefRenderHandler = this
    fun getRequestHandler(): CefRequestHandler = this
    fun getWindowHandler(): CefWindowHandler = this
}