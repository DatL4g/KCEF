package dev.datlag.kcef

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefSchemeHandlerFactory
import org.cef.handler.CefResourceHandler
import org.cef.network.CefRequest
import java.util.*

internal class KCEFFileSchemeHandlerFactory : CefSchemeHandlerFactory {
    override fun create(
        browser: CefBrowser?,
        frame: CefFrame?,
        schemeName: String?,
        request: CefRequest?
    ): CefResourceHandler? {
        if (!schemeName.equals(FILE_SCHEME_NAME)) {
            return null
        }

        val url = request?.url?.let(::normalizeUrl) ?: return null

        val map = browser?.let { LOAD_HTML_REQUEST[it] } ?: LOAD_HTML_CLIENT_REQUEST
        if (map.isNotEmpty()) {
            val html = map[url]
            if (!html.isNullOrEmpty()) {
                return KCEFLoadHtmlResourceHandler(html)
            }
        }

        return null
    }

    companion object {
        const val FILE_SCHEME_NAME = "file"
        const val SCHEME_SEPARATOR = "://"
        private const val HTML_URL_PREFIX = "$FILE_SCHEME_NAME$SCHEME_SEPARATOR/kcefbrowser/"

        // browser: url, html
        private val LOAD_HTML_REQUEST: MutableMap<CefBrowser, MutableMap<String, String>> = WeakHashMap()
        private val LOAD_HTML_CLIENT_REQUEST: MutableMap<String, String> = Collections.synchronizedMap<String, String>(
            emptyMap()
        ).toMutableMap()

        fun registerLoadHtmlRequest(browser: CefBrowser, html: String, url: String): String {
            val origUrl = normalizeUrl(url)
            val fileUrl = createFileUrl(origUrl)
            initMap(browser)[fileUrl] = html
            return fileUrl
        }

        fun registerLoadHtmlRequest(html: String, url: String): String {
            val origUrl = normalizeUrl(url)
            val fileUrl = createFileUrl(origUrl)
            LOAD_HTML_CLIENT_REQUEST[fileUrl] = html
            return fileUrl
        }

        private fun initMap(browser: CefBrowser): MutableMap<String, String> {
            var map = LOAD_HTML_REQUEST[browser]
            if (map == null) {
                synchronized(LOAD_HTML_REQUEST) {
                    map = LOAD_HTML_REQUEST[browser]
                    if (map == null) {
                        LOAD_HTML_REQUEST[browser] = Collections.synchronizedMap<String, String>(
                            emptyMap()
                        ).toMutableMap().also {
                            map = it
                        }
                    }
                }
            }
            return map!!
        }

        private fun createFileUrl(url: String): String {
            if (url.startsWith(FILE_SCHEME_NAME + SCHEME_SEPARATOR)) {
                return url
            }

            return normalizeUrl("$HTML_URL_PREFIX${(0..Int.MAX_VALUE).random()}#url=$url")
        }

        private fun normalizeUrl(url: String): String {
            return url.replace("/$", "")
        }
    }
}