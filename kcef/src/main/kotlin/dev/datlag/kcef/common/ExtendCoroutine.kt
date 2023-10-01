package dev.datlag.kcef.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

internal fun <T> scopeCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: Throwable) {
    if (e is CancellationException) {
        throw e
    }
    Result.failure(e)
}

internal suspend fun <T> suspendCatching(block: suspend CoroutineScope.() -> T): Result<T> = coroutineScope {
    try {
        Result.success(
            block(this)
        )
    } catch (e: Throwable) {
        if (e is CancellationException) {
            throw e
        }
        Result.failure(e)
    }
}
