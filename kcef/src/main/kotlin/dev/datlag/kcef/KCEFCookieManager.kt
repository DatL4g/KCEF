package dev.datlag.kcef

import dev.datlag.kcef.common.scopeCatching
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.cef.callback.CefCookieVisitor
import org.cef.network.CefCookie
import org.cef.network.CefCookieManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class KCEFCookieManager @JvmOverloads constructor(
    private val _cookieManager: CefCookieManager? = scopeCatching {
        CefCookieManager.getGlobalManager()
    }.getOrNull()
) {

    private val cookieManager
        get() = _cookieManager ?: CefCookieManager.getGlobalManager()

    /**
     * Get each cookie in a [callback]
     *
     * @param url results are filtered by the given url scheme, host, domain and path.
     * @param includeHttpOnly if true HTTP-only cookies will also be included in the results.
     * @param callback the [CookieCallback] for each cookie
     *
     * @return false if cookies cannot be accessed.
     */
    @JvmOverloads
    fun getCookies(
        url: String? = null,
        includeHttpOnly: Boolean = false,
        callback: CookieCallback
    ): Boolean {
        return if (!url.isNullOrEmpty()) {
            cookieManager.visitUrlCookies(url, includeHttpOnly, CefCookieVisitor { cookie, _, _, _ ->
                cookie?.let { callback(it) }
                return@CefCookieVisitor true
            })
        } else {
            cookieManager.visitAllCookies(CefCookieVisitor { cookie, _, _, _ ->
                cookie?.let { callback(it) }
                return@CefCookieVisitor true
            })
        }
    }

    /**
     * Collect cookies and return the list afterward
     *
     * @param url results are filtered by the given url scheme, host, domain and path.
     * @param includeHttpOnly if true HTTP-only cookies will also be included in the results.
     * @param delay the time to wait between [predicate] checks in milliseconds
     * @param predicate return true as long as you want to collect
     *
     * @return list of [CefCookie] collected while [predicate]
     */
    @JvmOverloads
    suspend fun getCookiesWhile(
        url: String? = null,
        includeHttpOnly: Boolean = false,
        delay: Long = 200,
        predicate: CookiesWhile = CookiesWhile { delayIteration, _ -> delayIteration <= 0 }
    ): List<CefCookie> = coroutineScope {
        val cookies: MutableList<CefCookie> = mutableListOf()
        var iteration: Int = 0

        val result = getCookies(url, includeHttpOnly) { c ->
            cookies.add(c)
        }
        while (predicate(iteration++, cookies.size) && result) {
            delay(delay)
        }
        cookies
    }

    /**
     * Blocking equivalent of [getCookiesWhile]
     *
     * @see [getCookiesWhile]
     */
    @JvmOverloads
    fun getCookiesWhileBlocking(
        url: String? = null,
        includeHttpOnly: Boolean = false,
        delay: Long = 200,
        predicate: CookiesWhile = CookiesWhile { delayIteration, _ -> delayIteration <= 0 }
    ) = runBlocking {
        getCookiesWhile(url, includeHttpOnly, delay, predicate)
    }

    /**
     * Set cookie for a given url
     *
     * @param url the cookie url
     * @param cookie the cookie attributes
     * @return false if an invalid URL is specified or if cookies cannot be flushed.
     */
    suspend fun setCookie(url: String, cookie: CefCookie): Boolean {
        return if (cookieManager.setCookie(url, cookie)) {
            flushStore()
        } else {
            false
        }
    }

    /**
     * Blocking equivalent of [setCookie]
     *
     * @see [setCookie]
     */
    fun setCookieBlocking(url: String, cookie: CefCookie) = runBlocking {
        setCookie(url, cookie)
    }

    /**
     * Delete all cookies that match the specified parameters.
     *
     * * If both [url] and [cookieName] values are specified all host and domain cookies matching both will be deleted.
     * * If only [url] is specified all host cookies (but not domain cookies) irrespective of path will be deleted.
     * * If [url] is empty all cookies for all hosts and domains will be deleted.
     *
     * @param url The cookie URL to delete or null.
     * @param cookieName The cookie name to delete or null.
     * @return false if a non-empty invalid URL is specified or if cookies cannot be flushed.
     */
    @JvmOverloads
    suspend fun deleteCookies(url: String? = null, cookieName: String? = null): Boolean {
        return if (cookieManager.deleteCookies(url, cookieName)) {
            flushStore()
        } else {
            if (!url.isNullOrEmpty()) {
                cookieManager.visitUrlCookies(url, true, CefCookieVisitor { cookie, _, _, delete ->
                    if (cookieName.isNullOrEmpty()) {
                        delete.set(true)
                    } else {
                        if (cookie.name.equals(cookieName)) {
                            delete.set(true)
                        }
                    }
                    false
                })
            } else {
                cookieManager.visitAllCookies { _, _, _, delete ->
                    delete.set(true)
                    false
                }
            }
        }
    }

    /**
     * Blocking equivalent of [deleteCookies]
     *
     * @see [deleteCookies]
     */
    @JvmOverloads
    fun deleteCookiesBlocking(url: String? = null, cookieName: String? = null) = runBlocking {
        deleteCookies(url, cookieName)
    }

    /**
     * Shorthand for [deleteCookies] which deletes all cookies
     *
     * @see [deleteCookies]
     */
    suspend fun deleteAllCookies() = deleteCookies(null, null)

    /**
     * Blocking equivalent of [deleteAllCookies]
     *
     * @see [deleteAllCookies]
     */
    fun deleteAllCookiesBlocking() = runBlocking {
        deleteCookies(null, null)
    }

    private suspend fun flushStore(data: Boolean = true): Boolean = suspendCoroutine {
        if (!cookieManager.flushStore { it.resume(data) }) {
            it.resume(false)
        }
    }

    companion object {

        @JvmStatic
        val instance by lazy(LazyThreadSafetyMode.NONE) {
            KCEFCookieManager()
        }
    }

    fun interface CookieCallback {
        operator fun invoke(cookie: CefCookie)
    }

    fun interface CookiesWhile {
        operator fun invoke(delayIteration: Int, collectedAmount: Int): Boolean
    }
}