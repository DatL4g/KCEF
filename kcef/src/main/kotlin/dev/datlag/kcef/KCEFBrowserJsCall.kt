package dev.datlag.kcef

import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class KCEFBrowserJsCall(
    private val javaScriptExpression: String,
    private val browser: KCEFBrowser
) {

    suspend fun await(): String? = coroutineScope {
        val deferredJsExecutionResult = async(start = CoroutineStart.LAZY) {
            suspendCancellableCoroutine { continuation ->
                javaScriptExpression.wrapWithErrorHandling(
                    resultQuery = createResultHandlerQueryWithinScope(continuation),
                    errorQuery = createErrorHandlerQueryWithinScope(continuation)
                ).also { jsToRun ->
                    browser.executeJavaScript(jsToRun, "", 0)
                }
            }
        }

        deferredJsExecutionResult.await()
    }

    private fun CoroutineScope.createResultHandlerQueryWithinScope(continuation: Continuation<String?>) =
        createQueryWithinScope().apply {
            addHandler { result ->
                continuation.resume(result)
                null
            }
        }

    private fun CoroutineScope.createErrorHandlerQueryWithinScope(continuation: Continuation<String?>) =
        createQueryWithinScope().apply {
            addHandler { errorMessage ->
                continuation.resumeWithException(KCEFException.JsCallError(errorMessage ?: "Unknown error"))
                null
            }
        }

    private fun CoroutineScope.createQueryWithinScope(): KCEFJSQuery = KCEFJSQuery.create(browser).also { query ->
        coroutineContext.job.invokeOnCompletion {
            query.dispose()
        }
    }

    private fun String.containsLineBreak(): Boolean {
        return this.any {
            it.isLineBreak()
        }
    }

    private fun Char.isLineBreak(): Boolean {
        return this == '\n' || this == '\r'
    }

    private fun String.asFunctionBody(): String = let { expression ->
        when {
            expression.containsLineBreak() -> expression
            expression.trim().startsWith("return", false) -> expression
            else -> "return $expression"
        }
    }

    private fun String.wrapWithErrorHandling(resultQuery: KCEFJSQuery, errorQuery: KCEFJSQuery) = """
      function payload() {
          ${asFunctionBody()}
      }

      try {
          let result = payload();

          // call back the related JBCefJSQuery
          window.${resultQuery.funcName}({
              request: "" + result,
              onSuccess: function (response) {
                  // do nothing
              }, onFailure: function (error_code, error_message) {
                  // do nothing
              }
          });
      } catch (e) {
          // call back the related error handling JBCefJSQuery
          window.${errorQuery.funcName}({
              request: "" + e,
              onSuccess: function (response) {
                  // do nothing
              }, onFailure: function (error_code, error_message) {
                  // do nothing
              }
          });
      }
    """.trimIndent()
}