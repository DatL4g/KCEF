package dev.datlag.kcef

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.browser.CefMessageRouter.CefMessageRouterConfig
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandler
import org.cef.handler.CefMessageRouterHandlerAdapter
import java.util.*


internal class KCEFJSQuery(
    private val client: KCEFClient,
    private val func: JSQueryFunc
) {

    constructor(browser: KCEFBrowser, func: JSQueryFunc) : this(browser.client, func)

    val funcName: String
        get() = func.funcName

    private val handlerMap: MutableMap<ResponseHandler, CefMessageRouterHandler>
        = Collections.synchronizedMap<ResponseHandler, CefMessageRouterHandler>(emptyMap()).toMutableMap()

    @JvmOverloads
    fun inject(
        queryResult: String?,
        onSuccessCallback: String = "function(response) {}",
        onFailureCallback: String = "function(error_code, error_message) {}"
    ): String {
        val result = if (queryResult.isNullOrEmpty()) "''" else queryResult
        return "window." + funcName +
                "({request: '' + " + result + "," +
                "onSuccess: " + onSuccessCallback + "," +
                "onFailure: " + onFailureCallback +
                "});"
    }

    fun addHandler(handler: ResponseHandler) {
        val cefHandler = object : CefMessageRouterHandlerAdapter() {
            override fun onQuery(
                browser: CefBrowser?,
                frame: CefFrame?,
                queryId: Long,
                request: String?,
                persistent: Boolean,
                callback: CefQueryCallback?
            ): Boolean {
                val response = handler(request)
                if (callback != null && response != null) {
                    if (response.isSuccess && response.hasResponse) {
                        callback.success(response.response)
                    } else {
                        callback.failure(response.errorCode, response.errorMessage)
                    }
                } else {
                    callback?.success("")
                }
                return true
            }
        }

        func.router.addHandler(cefHandler, false)
        handlerMap[handler] = cefHandler
    }

    fun removeHandler(handler: ResponseHandler) {
        val cefHandler = handlerMap.remove(handler)
        if (cefHandler != null) {
            func.router.removeHandler(cefHandler)
        }
    }

    fun clearHandlers() {
        handlerMap.keys.forEach(::removeHandler)
    }

    fun dispose() {
        if (func.isSlot) {
            val pool = client.jsQueryPool
            clearHandlers()
            pool.releaseUsedSlot(func)
            return
        }
        client.removeMessageRouter(func.router)
        func.router.dispose()
        handlerMap.clear()
    }

    companion object {
        fun create(browser: KCEFBrowser): KCEFJSQuery {
            fun createQuery() = KCEFJSQuery(browser, JSQueryFunc(browser))

            if (!browser.isCreated()) {
                return createQuery()
            }

            val pool = browser.client.jsQueryPool
            val slot = pool.useFreeSlot()
            if (slot != null) {
                return KCEFJSQuery(browser, slot)
            }
            return createQuery()
        }
    }

    internal class JSQueryFunc(
        client: KCEFClient,
        index: Int = client.nextJSQueryIndex(),
        internal val isSlot: Boolean = false
    ) {

        val funcName: String
        val router: CefMessageRouter

        init {
            val postFix = "${client.hashCode()}_${if (isSlot) "slot_" else ""}$index"
            this.funcName = "cefQuery_$postFix"
            val config = CefMessageRouterConfig()
            config.jsQueryFunction = this.funcName
            config.jsCancelFunction = "cefQuery_cancel_$postFix"
            this.router = CefMessageRouter.create(config)
            client.addMessageRouter(this.router)
        }

        constructor(
            browser: KCEFBrowser,
            index: Int = browser.client.nextJSQueryIndex(),
            isSlot: Boolean = false
        ) : this(browser.client)
    }

    internal data class Response @JvmOverloads constructor(
        val response: String?,
        val errorCode: Int = 0,
        val errorMessage: String? = null
    ) {
        val isSuccess
            get() = errorCode == 0

        val hasResponse
            get() = response != null
    }

}

internal typealias ResponseHandler = (String?) -> KCEFJSQuery.Response?