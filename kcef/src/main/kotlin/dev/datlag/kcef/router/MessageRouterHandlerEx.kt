package dev.datlag.kcef.router

import dev.datlag.kcef.common.systemProperty
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.browser.CefMessageRouter.CefMessageRouterConfig
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandlerAdapter

internal class MessageRouterHandlerEx(private val client: CefClient) : CefMessageRouterHandlerAdapter() {

    private val config = CefMessageRouterConfig("myQuery", "myQueryAbort")
    private var router: CefMessageRouter? = null

    override fun onQuery(
        browser: CefBrowser?,
        frame: CefFrame?,
        queryId: Long,
        request: String?,
        persistent: Boolean,
        callback: CefQueryCallback?
    ): Boolean {
        if (request?.startsWith("hasExtension") == true) {
            if (router != null) {
                callback?.success("")
            } else {
                callback?.failure(0, "")
            }
        } else if (request?.startsWith("enableExt") == true) {
            if (router != null) {
                callback?.failure(-1, "Already enabled")
            } else {
                router = CefMessageRouter.create(config, JavaVersionMessageRouter)
                client.addMessageRouter(router)
                callback?.success("")
            }
        } else if (request?.startsWith("disableExt") == true) {
            if (router == null) {
                callback?.failure(-2, "Already disabled")
            } else {
                client.removeMessageRouter(router)
                router?.dispose()
                router = null
                callback?.success("")
            }
        } else {
            return false
        }
        return true
    }

    internal data object JavaVersionMessageRouter : CefMessageRouterHandlerAdapter() {
        override fun onQuery(
            browser: CefBrowser?,
            frame: CefFrame?,
            queryId: Long,
            request: String?,
            persistent: Boolean,
            callback: CefQueryCallback?
        ): Boolean {
            if (request?.startsWith("jcefJava") == true) {
                callback?.success(systemProperty("java.version"))
                return true
            }
            return false
        }
    }
}