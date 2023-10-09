package dev.datlag.kcef.router

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandlerAdapter

internal class MessageRouterHandler : CefMessageRouterHandlerAdapter() {

    override fun onQuery(
        browser: CefBrowser?,
        frame: CefFrame?,
        queryId: Long,
        request: String?,
        persistent: Boolean,
        callback: CefQueryCallback?
    ): Boolean {
        if (request?.indexOf("BindingTest:") == 0) {
            val msg = request.substring(12)
            callback?.success(msg.reversed())
            return true
        }
        return false
    }
}