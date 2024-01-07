package dev.datlag.kcef

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefRequestContext
import org.cef.handler.CefCookieAccessFilter
import org.cef.handler.CefResourceHandler
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import org.cef.network.CefURLRequest

/**
 * This class extends a default [CefResourceRequestHandler] with possible modifications.
 */
open class KCEFResourceRequestHandler(
    private val defaultHandler: CefResourceRequestHandler?
) : CefResourceRequestHandler {

    private val fallback = object : CefResourceRequestHandlerAdapter() { }

    override fun getCookieAccessFilter(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?
    ): CefCookieAccessFilter {
        return defaultCookieAccessFilter(browser, frame, request)
    }

    fun defaultCookieAccessFilter(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?
    ): CefCookieAccessFilter {
        return (defaultHandler ?: fallback).getCookieAccessFilter(browser, frame, request)
    }

    override fun onBeforeResourceLoad(browser: CefBrowser?, frame: CefFrame?, request: CefRequest?): Boolean {
        return defaultBeforeResourceLoad(browser, frame, request)
    }

    fun defaultBeforeResourceLoad(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?
    ): Boolean {
        return (defaultHandler ?: fallback).onBeforeResourceLoad(browser, frame, request)
    }

    override fun getResourceHandler(browser: CefBrowser?, frame: CefFrame?, request: CefRequest?): CefResourceHandler {
        return defaultResourceHandler(browser, frame, request)
    }

    fun defaultResourceHandler(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?
    ): CefResourceHandler {
        return (defaultHandler ?: fallback).getResourceHandler(browser, frame, request)
    }

    override fun onResourceRedirect(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?,
        response: CefResponse?,
        new_url: StringRef?
    ) {
        defaultResourceRedirect(browser, frame, request, response, new_url)
    }

    fun defaultResourceRedirect(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?,
        response: CefResponse?,
        new_url: StringRef?
    ) {
        (defaultHandler ?: fallback).onResourceRedirect(browser, frame, request, response, new_url)
    }

    override fun onResourceResponse(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?,
        response: CefResponse?
    ): Boolean {
        return defaultResourceResponse(browser, frame, request, response)
    }

    fun defaultResourceResponse(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?,
        response: CefResponse?
    ): Boolean {
        return (defaultHandler ?: fallback).onResourceResponse(browser, frame, request, response)
    }

    override fun onResourceLoadComplete(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?,
        response: CefResponse?,
        status: CefURLRequest.Status?,
        receivedContentLength: Long
    ) {
        defaultResourceLoadComplete(browser, frame, request, response, status, receivedContentLength)
    }

    fun defaultResourceLoadComplete(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?,
        response: CefResponse?,
        status: CefURLRequest.Status?,
        receivedContentLength: Long
    ) {
        (defaultHandler ?: fallback).onResourceLoadComplete(browser, frame, request, response, status, receivedContentLength)
    }

    override fun onProtocolExecution(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?,
        allowOsExecution: BoolRef?
    ) {
        defaultProtocolExecution(browser, frame, request, allowOsExecution)
    }

    fun defaultProtocolExecution(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?,
        allowOsExecution: BoolRef?
    ) {
        (defaultHandler ?: fallback).onProtocolExecution(browser, frame, request, allowOsExecution)
    }

    companion object {
        fun getGlobalDefaultHandler(
            browser: CefBrowser?,
            frame: CefFrame?,
            request: CefRequest?,
            isNavigation: Boolean,
            isDownload: Boolean,
            requestInitiator: String?,
            disableDefaultHandling: BoolRef?
        ): CefResourceRequestHandler? {
            val globalContext = CefRequestContext.getGlobalContext()
            val handler = globalContext.handler ?: return null

            return handler.getResourceRequestHandler(
                browser,
                frame,
                request,
                isNavigation,
                isDownload,
                requestInitiator,
                disableDefaultHandling
            )
        }

        fun globalHandler(
            browser: CefBrowser?,
            frame: CefFrame?,
            request: CefRequest?,
            isNavigation: Boolean,
            isDownload: Boolean,
            requestInitiator: String?,
            disableDefaultHandling: BoolRef?
        ): KCEFResourceRequestHandler {
            return KCEFResourceRequestHandler(
                getGlobalDefaultHandler(
                    browser,
                    frame,
                    request,
                    isNavigation,
                    isDownload,
                    requestInitiator,
                    disableDefaultHandling
                )
            )
        }
    }
}