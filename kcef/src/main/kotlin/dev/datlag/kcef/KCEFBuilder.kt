package dev.datlag.kcef

import com.jetbrains.cef.JCefAppConfig
import dev.datlag.kcef.common.*
import dev.datlag.kcef.common.deleteDir
import dev.datlag.kcef.common.existsSafely
import dev.datlag.kcef.common.mkdirsSafely
import dev.datlag.kcef.common.unquarantine
import dev.datlag.kcef.step.extract.TarGzExtractor
import dev.datlag.kcef.step.fetch.PackageDownloader
import dev.datlag.kcef.step.init.CefInitializer
import org.cef.CefApp
import org.cef.CefSettings
import org.cef.CefSettings.ColorType
import java.io.File

class KCEFBuilder {

    internal var installDir: File = File("jcef-bundle")
    private var progress: InitProgress = InitProgress.Builder().build()

    private var settings: Settings = scopeCatching {
        Settings.fromJcefSettings(JCefAppConfig.getInstance().cefSettings)
    }.getOrNull() ?: Settings()

    private var args: MutableList<String> = scopeCatching {
        JCefAppConfig.getInstance().appArgsAsList.filterNotNull().toMutableList()
    }.getOrNull() ?: mutableListOf()

    private var releaseTag: String? = null
    private var downloadBufferSize: Long = 16 * 1024
    private var extractBufferSize: Long = 4096

    private var instance: CefApp? = null
    private val lock = Object()
    private var building = false
    private var installed = false
    private var downloading = false

    fun installDir(dir: File) = apply {
        this.installDir = dir
    }

    fun progress(listener: InitProgress) = apply {
        this.progress = listener
    }

    fun progress(builder: InitProgress.Builder.() -> Unit) = apply {
        this.progress = InitProgress.Builder().apply(builder).build()
    }

    fun settings(settings: Settings) = apply {
        this.settings = settings
    }

    fun settings(builder: Settings.() -> Unit) = apply {
        this.settings = settings.apply(builder)
    }

    fun args(vararg args: String) = apply {
        this.args.clear()
        this.args.addAll(args)
    }

    fun addArgs(vararg args: String) = apply {
        this.args.addAll(args)
    }

    fun release(tag: String) = apply {
        this.releaseTag = tag
    }

    fun release(latest: Boolean) = apply {
        if (latest) {
            this.releaseTag = null
        }
    }

    fun downloadBuffer(size: Number) = apply {
        if (size.toLong() > 0L) {
            downloadBufferSize = size.toLong()
        }
    }

    fun extractBuffer(size: Number) = apply {
        if (size.toLong() > 0L) {
            extractBufferSize = size.toLong()
        }
    }

    internal suspend fun install() = apply {
        if (this.installed) {
            return this
        }

        this.progress.locating()
        val installOk = File(installDir, "install.lock").existsSafely()
        if (!installOk) {
            installDir.deleteDir()

            if (!this.installDir.mkdirsSafely()) {
                throw CefException.InstallationDirectory
            }

            progress.downloading(0F)
            val downloadedFile = PackageDownloader.downloadPackage(
                releaseTag,
                installDir,
                progress,
                downloadBufferSize
            )

            this.progress.extracting()
            TarGzExtractor.extract(
                this.installDir,
                downloadedFile,
                extractBufferSize
            )

            TarGzExtractor.move(
                installDir
            )

            this.progress.install()
            if (Platform.getCurrentPlatform().os.isMacOSX) {
                this.installDir.unquarantine()
            }

            if (!File(this.installDir, "install.lock").createSafely()) {
                throw CefException.InstallationLock
            }
        }
        this.installed = true
    }

    @Throws(CefException::class)
    internal suspend fun build(): CefApp {
        this.instance?.let { return it }

        synchronized (lock) {
            if (building) {
                //Check if instance was not created in the meantime
                //to prevent race conditions
                if (this.instance == null) {
                    //Wait until building completed on another thread
                    lock.wait();
                }
                this.instance?.let { return it }
            }
            this.building = true
        }
        install()
        this.progress.initializing()
        synchronized(lock) {
            // Setting the instance has to occur in the synchronized block to prevent race conditions
            this.instance = CefInitializer.initialize(this.installDir, args, settings.toJcefSettings())
            //Add shutdown hook to attempt disposing our instance on jvm exit
            Runtime.getRuntime().addShutdownHook(Thread { this.instance?.dispose() })
            //Notify progress handler
            this.instance?.onInitialization { state ->
                if (state == CefApp.CefAppState.INITIALIZED) {
                    this.progress.initialized()
                }
            }
            //Resume waiting threads
            lock.notifyAll()
        }
        return this.instance!!
    }

    data class Settings(
        /**
         * The location where cache data will be stored on disk. If empty an in-memory
         * cache will be used for some features and a temporary disk cache for others.
         * HTML5 databases such as localStorage will only persist across sessions if a
         * cache path is specified.
         */
        var cachePath: String? = null,

        /**
         * Opaque background color used for accelerated content. By default the
         * background color will be white. Only the RGB compontents of the specified
         * value will be used. The alpha component must greater than 0 to enable use
         * of the background color but will be otherwise ignored.
         */
        var backgroundColor: ColorType? = null,

        /**
         * The path to a separate executable that will be launched for sub-processes.
         * By default the browser process executable is used. See the comments on
         * CefExecuteProcess() for details. Also configurable using the
         * "browser-subprocess-path" command-line switch.
         */
        var browserSubProcessPath: String? = null,

        /**
         * Set to true to disable configuration of browser process features using
         * standard CEF and Chromium command-line arguments. Configuration can still
         * be specified using CEF data structures or via the
         * CefApp::OnBeforeCommandLineProcessing() method.
         */
        var commandLineArgsDisabled: Boolean = false,
        var cookieableSchemesExcludeDefaults: Boolean = false,
        var cookieableSchemesList: String? = null,

        /**
         * Custom flags that will be used when initializing the V8 JavaScript engine.
         * The consequences of using custom flags may not be well tested. Also
         * configurable using the "js-flags" command-line switch.
         */
        var javascriptFlags: String? = null,

        /**
         * The locale string that will be passed to Blink. If empty the default
         * locale of "en-US" will be used. This value is ignored on Linux where locale
         * is determined using environment variable parsing with the precedence order:
         * LANGUAGE, LC_ALL, LC_MESSAGES and LANG. Also configurable using the "lang"
         * command-line switch.
         */
        var locale: String? = null,

        /**
         * The fully qualified path for the locales directory. If this value is empty
         * the locales directory must be located in the module directory. This value
         * is ignored on Mac OS X where pack files are always loaded from the app
         * bundle Resources directory. Also configurable using the "locales-dir-path"
         * command-line switch.
         */
        var localesDirPath: String? = null,

        /**
         * The directory and file name to use for the debug log. If empty, the
         * default name of "debug.log" will be used and the file will be written
         * to the application directory. Also configurable using the "log-file"
         * command-line switch.
         */
        var logFile: String? = null,

        /**
         * The log severity. Only messages of this severity level or higher will be
         * logged. Also configurable using the "log-severity" command-line switch with
         * a value of "verbose", "info", "warning", "error", "error-report" or
         * "disable".
         */
        var logSeverity: LogSeverity = LogSeverity.Default,

        /**
         * Set to true to disable loading of pack files for resources and locales.
         * A resource bundle handler must be provided for the browser and render
         * processes via CefApp::GetResourceBundleHandler() if loading of pack files
         * is disabled. Also configurable using the "disable-pack-loading" command-
         * line switch.
         */
        var packLoadingDisabled: Boolean = false,

        /**
         * To persist session cookies (cookies without an expiry date or validity
         * interval) by default when using the global cookie manager set this value to
         * true. Session cookies are generally intended to be transient and most Web
         * browsers do not persist them. A |cache_path| value must also be specified to
         * enable this feature. Also configurable using the "persist-session-cookies"
         * command-line switch.
         */
        var persistSessionCookies: Boolean = false,

        /**
         * Set to a value between 1024 and 65535 to enable remote debugging on the
         * specified port. For example, if 8080 is specified the remote debugging URL
         * will be http: *localhost:8080. CEF can be remotely debugged from any CEF or
         * Chrome browser window. Also configurable using the "remote-debugging-port"
         * command-line switch.
         */
        var remoteDebuggingPort: Int = 0,

        /**
         * The fully qualified path for the resources directory. If this value is
         * empty the cef.pak and/or devtools_resources.pak files must be located in
         * the module directory on Windows/Linux or the app bundle Resources directory
         * on Mac OS X. Also configurable using the "resources-dir-path" command-line
         * switch.
         */
        var resourcesDirPath: String? = null,

        /**
         * The number of stack trace frames to capture for uncaught exceptions.
         * Specify a positive value to enable the CefV8ContextHandler::
         * OnUncaughtException() callback. Specify 0 (default value) and
         * OnUncaughtException() will not be called. Also configurable using the
         * "uncaught-exception-stack-size" command-line switch.
         */
        var uncaughtExceptionStackSize: Int = 0,

        /**
         * Value that will be returned as the User-Agent HTTP header. If empty the
         * default User-Agent string will be used. Also configurable using the
         * "user-agent" command-line switch.
         */
        var userAgent: String? = null,

        /**
         * Value that will be inserted as the product portion of the default
         * User-Agent string. If empty the Chromium product version will be used. If
         * |userAgent| is specified this value will be ignored. Also configurable
         * using the "user_agent_product" command-line switch.
         */
        var userAgentProduct: String? = null,

        /**
         * Set to true to enable windowless (off-screen) rendering support. Do not
         * enable this value if the application does not use windowless rendering as
         * it may reduce rendering performance on some systems.
         */
        var windowlessRenderingEnabled: Boolean = false,

        var noSandbox: Boolean = scopeCatching {
            JCefAppConfig.getInstance().cefSettings.no_sandbox
        }.getOrNull() ?: CefSettings().no_sandbox
    ) {
        /**
         * The log severity. Only messages of this severity level or higher will be
         * logged. Also configurable using the "log-severity" command-line switch with
         * a value of "verbose", "info", "warning", "error", "error-report" or
         * "disable".
         */
        fun logSeverity(severity: CefSettings.LogSeverity) = apply {
            logSeverity = LogSeverity.fromJCefSeverity(severity)
        }

        /**
         * Log severity levels.
         */
        sealed interface LogSeverity {

            /**
             * Default logging (currently INFO logging).
             */
            data object Default : LogSeverity

            /**
             * Verbose logging.
             */
            data object Verbose : LogSeverity

            /**
             * INFO logging.
             */
            data object Info : LogSeverity

            /**
             * WARNING logging.
             */
            data object Warning : LogSeverity

            /**
             * ERROR logging.
             */
            data object Error : LogSeverity

            /**
             * FATAL logging.
             */
            data object Fatal : LogSeverity

            /**
             * Completely disable logging.
             */
            data object Disable : LogSeverity

            fun toJCefSeverity(): CefSettings.LogSeverity = when (this) {
                Default -> CefSettings.LogSeverity.LOGSEVERITY_DEFAULT
                Verbose -> CefSettings.LogSeverity.LOGSEVERITY_VERBOSE
                Info -> CefSettings.LogSeverity.LOGSEVERITY_INFO
                Warning -> CefSettings.LogSeverity.LOGSEVERITY_WARNING
                Error -> CefSettings.LogSeverity.LOGSEVERITY_ERROR
                Fatal -> CefSettings.LogSeverity.LOGSEVERITY_FATAL
                Disable -> CefSettings.LogSeverity.LOGSEVERITY_DISABLE
                else -> CefSettings.LogSeverity.LOGSEVERITY_DEFAULT
            }

            companion object {
                internal fun fromJCefSeverity(severity: CefSettings.LogSeverity): LogSeverity =
                    when (severity) {
                        CefSettings.LogSeverity.LOGSEVERITY_DEFAULT -> Default
                        CefSettings.LogSeverity.LOGSEVERITY_VERBOSE -> Verbose
                        CefSettings.LogSeverity.LOGSEVERITY_INFO -> Info
                        CefSettings.LogSeverity.LOGSEVERITY_WARNING -> Warning
                        CefSettings.LogSeverity.LOGSEVERITY_ERROR -> Error
                        CefSettings.LogSeverity.LOGSEVERITY_FATAL -> Fatal
                        CefSettings.LogSeverity.LOGSEVERITY_DISABLE -> Disable
                        else -> Default
                    }
            }
        }

        internal fun toJcefSettings() = CefSettings().apply {
            this.cache_path = this@Settings.cachePath
            this.background_color = this@Settings.backgroundColor
            this.browser_subprocess_path = this@Settings.browserSubProcessPath
            this.command_line_args_disabled = this@Settings.commandLineArgsDisabled
            this.cookieable_schemes_exclude_defaults = this@Settings.cookieableSchemesExcludeDefaults
            this.cookieable_schemes_list = this@Settings.cookieableSchemesList
            this.javascript_flags = this@Settings.javascriptFlags
            this.locale = this@Settings.locale
            this.locales_dir_path = this@Settings.localesDirPath
            this.log_file = this@Settings.logFile
            this.log_severity = this@Settings.logSeverity.toJCefSeverity()
            this.pack_loading_disabled = this@Settings.packLoadingDisabled
            this.persist_session_cookies = this@Settings.persistSessionCookies
            this.remote_debugging_port = this@Settings.remoteDebuggingPort
            this.resources_dir_path = this@Settings.resourcesDirPath
            this.uncaught_exception_stack_size = this@Settings.uncaughtExceptionStackSize
            this.user_agent = this@Settings.userAgent
            this.user_agent_product = this@Settings.userAgentProduct
            this.windowless_rendering_enabled = this@Settings.windowlessRenderingEnabled
            this.no_sandbox = this@Settings.noSandbox
        }

        companion object {
            internal fun fromJcefSettings(settings: CefSettings) = Settings(
                cachePath = settings.cache_path,
                backgroundColor = settings.background_color,
                browserSubProcessPath = settings.browser_subprocess_path,
                commandLineArgsDisabled = settings.command_line_args_disabled,
                cookieableSchemesExcludeDefaults = settings.cookieable_schemes_exclude_defaults,
                cookieableSchemesList = settings.cookieable_schemes_list,
                javascriptFlags = settings.javascript_flags,
                locale = settings.locale,
                localesDirPath = settings.locales_dir_path,
                logFile = settings.log_file,
                logSeverity = LogSeverity.fromJCefSeverity(settings.log_severity),
                packLoadingDisabled = settings.pack_loading_disabled,
                persistSessionCookies = settings.persist_session_cookies,
                remoteDebuggingPort = settings.remote_debugging_port,
                resourcesDirPath = settings.resources_dir_path,
                uncaughtExceptionStackSize = settings.uncaught_exception_stack_size,
                userAgent = settings.user_agent,
                userAgentProduct = settings.user_agent_product,
                windowlessRenderingEnabled = settings.windowless_rendering_enabled,
                noSandbox = settings.no_sandbox
            )
        }
    }
}

fun kcef(builder: KCEFBuilder.() -> Unit) = KCEFBuilder().apply(builder)