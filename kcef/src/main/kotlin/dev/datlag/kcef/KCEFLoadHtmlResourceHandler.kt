package dev.datlag.kcef

import dev.datlag.kcef.common.scopeCatching
import org.cef.callback.CefCallback
import org.cef.handler.CefResourceHandlerAdapter
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.io.InputStream
import kotlin.math.min

class KCEFLoadHtmlResourceHandler constructor(
    private val inputStream: InputStream
) : CefResourceHandlerAdapter() {

    constructor(html: String) : this(html.byteInputStream())

    override fun processRequest(request: CefRequest?, callback: CefCallback?): Boolean {
        callback?.Continue()
        return true
    }

    override fun getResponseHeaders(response: CefResponse?, responseLength: IntRef?, redirectUrl: StringRef?) {
        response?.mimeType = "text/html"
        response?.status = 200
    }

    override fun readResponse(
        dataOut: ByteArray?,
        bytesToRead: Int,
        bytesRead: IntRef?,
        callback: CefCallback?
    ): Boolean {
        inputStream.use {
            val availableSize =scopeCatching {
                it.available()
            }.getOrNull() ?: 0

            if (availableSize > 0) {
                var byteSizeToRead = min(bytesToRead, availableSize)
                byteSizeToRead = dataOut?.let { data ->
                    it.read(data, 0, byteSizeToRead)
                } ?: byteSizeToRead
                bytesRead?.set(byteSizeToRead)
                return true
            }
        }
        bytesRead?.set(0)
        return false
    }
}