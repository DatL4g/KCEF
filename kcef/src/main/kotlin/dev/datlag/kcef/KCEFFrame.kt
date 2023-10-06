package dev.datlag.kcef

import org.cef.browser.CefFrame
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class KCEFFrame internal constructor(
    private val client: KCEFClient,
    private val frame: CefFrame
) : CefFrame by frame {

    /**
     * Execute the Javascript code and get the response in [callback].
     *
     * @param javaScriptExpression the passed JavaScript code should be either:
     * * a valid single-line JavaScript expression
     * * a valid multi-line function-body with at least one "return" statement
     * @param callback a [EvaluateJavascriptCallback] listener to handle the response
     */
    fun evaluateJavaScript(javaScriptExpression: String, callback: EvaluateJavascriptCallback) {
        val functionName = client.evaluateJSHandler.queryFunction(client)
        client.evaluateJSHandler.addHandler(functionName) { response ->
            callback(response)
            null
        }

        val executeJs = KCEFJSHandler.wrapWithErrorHandling(javaScriptExpression, functionName)
        executeJavaScript(executeJs, "", 0)
    }

    /**
     * Execute the Javascript code and wait for a response.
     * This [suspend] equivalent is useful if you want to work with timeouts for example.
     *
     * @param javaScriptExpression the passed JavaScript code should be either:
     * * a valid single-line JavaScript expression
     * * a valid multi-line function-body with at least one "return" statement
     *
     * @see evaluateJavaScript
     * @return a nullable [String] which is the response
     */
    suspend fun evaluateJavaScript(javaScriptExpression: String): String? = suspendCoroutine { continuation ->
        evaluateJavaScript(javaScriptExpression) { response ->
            continuation.resume(response)
        }
    }

    fun interface EvaluateJavascriptCallback {
        operator fun invoke(response: String?)
    }

}