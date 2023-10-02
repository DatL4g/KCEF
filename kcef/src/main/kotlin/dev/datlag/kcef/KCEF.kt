package dev.datlag.kcef

import dev.datlag.kcef.common.existsSafely
import dev.datlag.kcef.common.suspendCatching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.cef.CefApp
import org.cef.CefClient
import java.io.File
import kotlin.properties.Delegates

data object KCEF {

    private val state: MutableStateFlow<State> = MutableStateFlow(State.New)
    private var cefApp by Delegates.notNull<CefApp>()

    @JvmOverloads
    suspend fun init(
        builder: KCEFBuilder.() -> Unit,
        onError: (Throwable?) -> Unit = { },
        onRestartRequired: () -> Unit = { }
    ) = init(
        builder = KCEFBuilder().apply(builder),
        onError = onError,
        onRestartRequired = onRestartRequired
    )

    @JvmOverloads
    fun initBlocking(
        builder: KCEFBuilder.() -> Unit,
        onError: (Throwable?) -> Unit = { },
        onRestartRequired: () -> Unit = { }
    ) = runBlocking {
        init(builder, onError, onRestartRequired)
    }

    @JvmOverloads
    suspend fun init(
        builder: KCEFBuilder,
        onError: (Throwable?) -> Unit = { },
        onRestartRequired: () -> Unit = { }
    ) {
        val currentBuilder = when (state.value) {
            State.Disposed -> throw CefException.Disposed
            State.Initializing, State.Initialized -> null
            State.New, is State.Error -> {
                state.emit(State.Initializing)
                builder
            }
        } ?: return

        val installOk = File(builder.installDir, "install.lock").existsSafely()

        if (installOk) {
            val result = suspendCatching {
                currentBuilder.build()
            }
            setInitResult(result)
            result.exceptionOrNull()?.let(onError)
        } else {
            val installResult = suspendCatching {
                builder.install()
            }
            installResult.exceptionOrNull()?.let {
                setInitResult(Result.failure(it))
                onError(it)
            }

            val result = suspendCatching {
                builder.build()
            }

            setInitResult(result)
            if (result.isFailure) {
                result.exceptionOrNull()?.let(onError)
                setInitResult(Result.failure(CefException.ApplicationRestartRequired))
                onRestartRequired.invoke()
            }
        }
    }

    @JvmOverloads
    fun initBlocking(
        builder: KCEFBuilder,
        onError: (Throwable?) -> Unit = { },
        onRestartRequired: () -> Unit = { }
    ) = runBlocking {
        init(builder, onError, onRestartRequired)
    }

    suspend fun newClient(): CefClient {
        return when (state.value) {
            State.New -> throw CefException.NotInitialized
            State.Disposed -> throw CefException.Disposed
            is State.Error -> throw CefException.Error((state.value as? State.Error)?.exception)
            State.Initialized -> cefApp.createClient()
            State.Initializing -> {
                state.first { it != State.Initializing }

                return newClient()
            }
        }
    }

    fun newClientBlocking(): CefClient = runBlocking {
        newClient()
    }

    @JvmOverloads
    suspend fun newClientOrNull(onError: (Throwable?) -> Unit = { }): CefClient? {
        return when (state.value) {
            State.New -> {
                onError(CefException.NotInitialized)
                null
            }
            State.Disposed -> {
                onError(CefException.Disposed)
                null
            }
            is State.Error -> {
                onError(CefException.Error((state.value as? State.Error)?.exception))
                null
            }
            State.Initialized -> cefApp.createClient()
            State.Initializing -> {
                state.first { it != State.Initializing }

                return newClientOrNull(onError)
            }
        }
    }

    @JvmOverloads
    fun newClientOrNullBlocking(onError: (Throwable?) -> Unit = { }): CefClient? = runBlocking {
        newClientOrNull(onError)
    }

    suspend fun dispose() {
        when (state.value) {
            State.New, State.Disposed, is State.Error -> return
            State.Initializing -> {
                state.first { it != State.Initializing }

                return dispose()
            }
            State.Initialized -> {
                state.emit(State.Disposed)
                cefApp.dispose()
            }
        }
    }

    fun disposeBlocking() = runBlocking {
        dispose()
    }

    private fun setInitResult(result: Result<CefApp>): Boolean {
        val nextState = if (result.isSuccess) {
            cefApp = result.getOrThrow()
            State.Initialized
        } else {
            State.Error(result.exceptionOrNull())
        }

        return state.compareAndSet(State.Initializing, nextState)
    }

    private sealed class State {
        data object New : State()
        data object Initializing : State()
        data object Initialized : State()
        data class Error(val exception: Throwable?) : State()
        data object Disposed : State()
    }
}