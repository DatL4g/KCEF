package dev.datlag.kcef

import dev.datlag.kcef.KCEF.InitError
import dev.datlag.kcef.KCEF.InitRestartRequired
import dev.datlag.kcef.KCEF.NewClientOrNullError
import dev.datlag.kcef.common.existsSafely
import dev.datlag.kcef.common.scopeCatching
import dev.datlag.kcef.common.suspendCatching
import dev.datlag.kcef.common.systemLoadLibrary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.cef.CefApp
import org.cef.CefSettings
import org.cef.handler.CefAppHandlerAdapter
import java.io.File
import kotlin.properties.Delegates

/**
 * Class used to initialize the JCef environment.
 *
 * Create a new [KCEFClient] after initialization easily.
 *
 * Dispose the JCef environment if you don't need it anymore.
 */
data object KCEF {

    private val state: MutableStateFlow<State> = MutableStateFlow(State.New)
    private var cefApp by Delegates.notNull<CefApp>()

    /**
     * Download, install and initialize CEF on the client.
     *
     * @param builder builder method to create a [KCEFBuilder] to use
     * @param onError an optional listener for errors
     * @param onRestartRequired an optional listener to be notified when the application needs a restart,
     * may happen on some platforms if CEF couldn't be initialized after downloading and installing.
     * @throws KCEFException.Disposed if you called [dispose] or [disposeBlocking] previously
     */
    @JvmStatic
    @JvmOverloads
    @Throws(KCEFException.Disposed::class)
    suspend fun init(
        builder: KCEFBuilder.() -> Unit,
        onError: InitError = InitError {  },
        onRestartRequired: InitRestartRequired = InitRestartRequired {  }
    ) = init(
        builder = KCEFBuilder().apply(builder),
        onError = onError,
        onRestartRequired = onRestartRequired
    )

    /**
     * Blocking equivalent of [init]
     *
     * @see init
     */
    @JvmStatic
    @JvmOverloads
    @Throws(KCEFException.Disposed::class)
    fun initBlocking(
        builder: KCEFBuilder.() -> Unit,
        onError: InitError = InitError {  },
        onRestartRequired: InitRestartRequired = InitRestartRequired {  }
    ) = runBlocking {
        init(builder, onError, onRestartRequired)
    }

    /**
     * Download, install and initialize CEF on the client.
     *
     * @param builder the [KCEFBuilder] to use
     * @param onError an optional listener for errors
     * @param onRestartRequired an optional listener to be notified when the application needs a restart,
     * may happen on some platforms if CEF couldn't be initialized after downloading and installing.
     * @throws KCEFException.Disposed if you called [dispose] or [disposeBlocking] previously
     */
    @JvmStatic
    @JvmOverloads
    @Throws(KCEFException.Disposed::class)
    suspend fun init(
        builder: KCEFBuilder,
        onError: InitError = InitError {  },
        onRestartRequired: InitRestartRequired = InitRestartRequired {  }
    ) {
        val currentBuilder = when (state.value) {
            State.Disposed -> throw KCEFException.Disposed
            State.Initializing, State.Initialized -> null
            State.New, is State.Error -> {
                state.emit(State.Initializing)
                builder
            }
        } ?: return

        CefApp.addAppHandler(currentBuilder.appHandler ?: AppHandler())

        if (initFromRuntime(currentBuilder.args)) {
            val result = suspendCatching {
                CefApp.getInstanceIfAny() ?: suspendCatching {
                    CefApp.getInstance(currentBuilder.settings.toJcefSettings())
                }.getOrNull() ?: CefApp.getInstance()
            }
            setInitResult(result)
            result.exceptionOrNull()?.let(onError::invoke)

            return
        }

        val installOk = File(currentBuilder.installDir, "install.lock").existsSafely()

        if (installOk) {
            val result = suspendCatching {
                currentBuilder.build()
            }
            setInitResult(result)
            result.exceptionOrNull()?.let(onError::invoke)
        } else {
            val installResult = suspendCatching {
                currentBuilder.install()
            }
            installResult.exceptionOrNull()?.let {
                setInitResult(Result.failure(it))
                onError(it)
            }

            val result = suspendCatching {
                currentBuilder.build()
            }

            setInitResult(result)
            if (result.isFailure) {
                result.exceptionOrNull()?.let(onError::invoke)
                setInitResult(Result.failure(KCEFException.ApplicationRestartRequired))
                onRestartRequired()
            }
        }
    }

    /**
     * Blocking equivalent of [init]
     *
     * @see init
     */
    @JvmStatic
    @JvmOverloads
    @Throws(KCEFException.Disposed::class)
    fun initBlocking(
        builder: KCEFBuilder,
        onError: InitError = InitError {  },
        onRestartRequired: InitRestartRequired = InitRestartRequired {  }
    ) = runBlocking {
        init(builder, onError, onRestartRequired)
    }

    /**
     * Mark CEF as initialized on the client.
     *
     * @param cefApp the initialized [CefApp] instance, used to create clients.
     */
    @KCEFAcknowledge
    @JvmStatic
    @JvmOverloads
    @Throws(UnsatisfiedLinkError::class, IllegalStateException::class)
    fun init(cefApp: CefApp = CefApp.getInstance()) {
        this.state.update { State.Initialized }
        this.cefApp = cefApp
    }

    /**
     * Create a new CefClient after CEF has been initialized.
     *
     * Waits for initialization if it isn't finished yet.
     *
     * @see init to initialize CEF
     * @throws KCEFException.NotInitialized if the [init] method have not been called.
     * @throws KCEFException.Disposed if you called [dispose] or [disposeBlocking] previously
     * @throws KCEFException.Error if any other error occurred during initialization
     * @return [KCEFClient] after initialization
     */
    @JvmStatic
    @Throws(
        KCEFException.NotInitialized::class,
        KCEFException.Disposed::class,
        KCEFException.Error::class
    )
    suspend fun newClient(): KCEFClient {
        return when (val current = state.value) {
            State.New -> throw KCEFException.NotInitialized
            State.Disposed -> throw KCEFException.Disposed
            is State.Error -> throw KCEFException.Error(current.exception)
            State.Initialized -> KCEFClient(cefApp.createClient())
            State.Initializing -> {
                state.first { it != State.Initializing }

                return newClient()
            }
        }
    }

    /**
     * Blocking equivalent of [newClient]
     *
     * @see newClient to initialize CEF
     */
    @JvmStatic
    @Throws(
        KCEFException.NotInitialized::class,
        KCEFException.Disposed::class,
        KCEFException.Error::class
    )
    fun newClientBlocking(): KCEFClient = runBlocking {
        newClient()
    }

    /**
     * Create a new CefClient after CEF has been initialized.
     *
     * Waits for initialization if it isn't finished yet.
     *
     * @see init to initialize CEF
     * @param onError an optional listener for any error occurred during initialization
     * @return [KCEFClient] after initialization or null if any error occurred
     */
    @JvmStatic
    @JvmOverloads
    suspend fun newClientOrNull(onError: NewClientOrNullError = NewClientOrNullError {  }): KCEFClient? {
        return when (val current = state.value) {
            State.New -> {
                onError(KCEFException.NotInitialized)
                null
            }
            State.Disposed -> {
                onError(KCEFException.Disposed)
                null
            }
            is State.Error -> {
                onError(KCEFException.Error(current.exception))
                null
            }
            State.Initialized -> KCEFClient(cefApp.createClient())
            State.Initializing -> {
                state.first { it != State.Initializing }

                return newClientOrNull(onError)
            }
        }
    }

    /**
     * Blocking equivalent of [newClientOrNull]
     *
     * @see newClientOrNull to initialize CEF
     */
    @JvmStatic
    @JvmOverloads
    fun newClientOrNullBlocking(onError: NewClientOrNullError = NewClientOrNullError {  }): KCEFClient? = runBlocking {
        newClientOrNull(onError)
    }

    /**
     * Create a new client if CEF has been initialized.
     *
     * @return [KCEFClient] if the initialization process already finished else null.
     */
    @JvmStatic
    fun newPossibleClient(): KCEFClient? {
        return when (state.value) {
            is State.Initialized -> KCEFClient(cefApp.createClient())
            else -> null
        }
    }

    /**
     * Dispose the [CefApp] instance if it is not needed anymore.
     * For example on exiting the application.
     *
     * Waits for initialization if it isn't finished yet
     */
    @JvmStatic
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

    /**
     * Blocking equivalent of [dispose]
     *
     * @see dispose to initialize CEF
     */
    @JvmStatic
    fun disposeBlocking() = runBlocking {
        dispose()
    }

    private fun initFromRuntime(cefArgs: Collection<String>): Boolean {
        systemLoadLibrary("jawt") || return false

        if (cefArgs.none { it.trim().equals("--disable-gpu", true) }) {
            systemLoadLibrary("EGL")
            systemLoadLibrary("GLESv2")
            systemLoadLibrary("vk_swiftshader")
        }

        systemLoadLibrary("libcef") || systemLoadLibrary("cef") || systemLoadLibrary("jcef") || return false

        return scopeCatching {
            CefApp.startup(cefArgs.toTypedArray())
        }.getOrNull() ?: false
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

    fun interface InitError {
        operator fun invoke(throwable: Throwable?)
    }

    fun interface InitRestartRequired {
        operator fun invoke()
    }

    fun interface NewClientOrNullError {
        operator fun invoke(throwable: Throwable?)
    }

    open class AppHandler @JvmOverloads constructor(
        args: Array<String> = emptyArray()
    ) : CefAppHandlerAdapter(args) {

        override fun onContextInitialized() {
            super.onContextInitialized()

            cefApp.registerSchemeHandlerFactory(
                KCEFFileSchemeHandlerFactory.FILE_SCHEME_NAME, "", KCEFFileSchemeHandlerFactory()
            )
        }
    }
}