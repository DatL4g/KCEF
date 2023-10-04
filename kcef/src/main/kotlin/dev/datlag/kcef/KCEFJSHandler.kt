package dev.datlag.kcef

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandlerAdapter

internal class KCEFJSHandler : CefMessageRouterHandlerAdapter() {

    private val handler: MutableMap<String, (String?) -> Response?> = mutableMapOf()

    override fun onQuery(
        browser: CefBrowser?,
        frame: CefFrame?,
        queryId: Long,
        request: String?,
        persistent: Boolean,
        callback: CefQueryCallback?
    ): Boolean {
        super.onQuery(browser, frame, queryId, request, persistent, callback)

        if (request != null) {
            val queryFunction = request.substringBefore('_', "")
            val handler = handler[queryFunction]

            if (queryFunction.isBlank() || handler == null) {
                return false
            }
            val actualRequest = request.substringAfter('_')
            val response = handler(actualRequest)

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

        return false
    }

    fun addHandler(queryFunction: String, handler: (String?) -> Response?) {
        this.handler[queryFunction] = handler
    }

    fun queryFunction(client: KCEFClient, index: Int = client.nextJSQueryIndex()): String {
        return "${client.hashCode()}-${index}"
    }

    companion object {
        private fun Char.isLineBreak(): Boolean {
            return this == '\n' || this == '\r'
        }

        private fun String.containsLineBreak(): Boolean {
            return this.any {
                it.isLineBreak()
            }
        }

        private fun String.asFunctionBody(): String = let { expression ->
            when {
                expression.containsLineBreak() -> expression
                expression.trim().startsWith("return", false) -> expression
                else -> "return $expression"
            }
        }

        internal fun wrapWithErrorHandling(script: String, functionName: String) = """
          function payload() {
              ${script.asFunctionBody()}
          }
    
          try {
              let result = payload();
    
              window.cefQueryEvaluate({
                  request: "${functionName}_" + result,
                  onSuccess: function (response) {
                  
                  }, onFailure: function (error_code, error_message) {
                  
                  }
              });
          } catch (e) {
              window.cefQueryEvaluateCancel({
                  request: "${functionName}_" + e,
                  onSuccess: function (response) {
                  
                  }, onFailure: function (error_code, error_message) {
                  
                  }
              });
          }
        """.trimIndent()
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