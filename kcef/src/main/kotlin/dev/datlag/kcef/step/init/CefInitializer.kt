package dev.datlag.kcef.step.init

import dev.datlag.kcef.CefException
import dev.datlag.kcef.Platform
import dev.datlag.kcef.common.scopeCatching
import dev.datlag.kcef.common.systemLoad
import dev.datlag.kcef.common.systemLoadLibrary
import dev.datlag.kcef.common.systemProperty
import org.cef.CefApp
import org.cef.CefSettings
import org.cef.SystemBootstrap
import java.io.File

internal data object CefInitializer {

    private const val JAVA_LIBRARY_PATH = "java.library.path"

    fun initialize(installDir: File, cefArgs: Collection<String>, cefSettings: CefSettings): CefApp {
        var path = systemProperty(JAVA_LIBRARY_PATH) ?: ""

        if (path.isNotEmpty() && !path.endsWith(File.pathSeparator)) {
            path += File.pathSeparator
        }

        path += installDir.canonicalPath
        systemProperty(JAVA_LIBRARY_PATH, path)

        loadLibrary(installDir, path, "jawt")
        loadLibrary(installDir, path, "EGL")
        loadLibrary(installDir, path, "GLESv2")

        SystemBootstrap.setLoader {
            loadLibrary(installDir, path, it)
        }

        val success = if (Platform.getCurrentPlatform().os.isMacOSX) {
            val newArgs = cefArgs.toMutableList()
            newArgs.add(
                0,
                "--framework-dir-path=${installDir.canonicalPath}/Chromium Embedded Framework.framework"
            )
            newArgs.add(
                0,
                "--main-bundle-path=${installDir.canonicalPath}/jcef Helper.app"
            )
            newArgs.add(
                0,
                "--browser-subprocess-path=${installDir.canonicalPath}/jcef Helper.app/Contents/MacOS/jcef Helper"
            )
            cefSettings.browser_subprocess_path = "${installDir.canonicalPath}/jcef Helper.app/Contents/MacOS/jcef Helper"
            CefApp.startup(newArgs.toTypedArray())
        } else {
            CefApp.startup(cefArgs.toTypedArray())
        }

        if (!success) {
            throw CefException.Startup
        }
        return CefApp.getInstanceIfAny() ?: scopeCatching {
            CefApp.getInstance(cefSettings)
        }.getOrNull() ?: CefApp.getInstance()
    }

    private fun loadLibrary(installDir: File, libraryPath: String, name: String): Boolean {
        val os = Platform.getCurrentPlatform().os
        val ending = when {
            os.isWindows -> ".dll"
            os.isLinux -> ".so"
            os.isMacOSX -> ".dylib"
            else -> ""
        }

        var libraryPathLibraryLoaded = true
        systemLoadLibrary(File(libraryPath, name)) {
            systemLoadLibrary(File(libraryPath, name + ending)) {
                libraryPathLibraryLoaded = false
            }
        }

        var libraryPathLoaded = true
        systemLoad(File(libraryPath, name)) {
            systemLoad(File(libraryPath, name + ending)) {
                libraryPathLoaded = false
            }
        }

        var installDirLibraryLoaded = true
        systemLoadLibrary(File(installDir, name)) {
            systemLoadLibrary(File(installDir, name + ending)) {
                installDirLibraryLoaded = false
            }
        }

        var installDirLoaded = true
        systemLoad(File(installDir, name)) {
            systemLoad(File(installDir, name + ending)) {
                installDirLoaded = false
            }
        }

        var libraryLoaded = true
        if (!libraryPathLibraryLoaded && !libraryPathLoaded && !installDirLibraryLoaded && !installDirLoaded) {
            systemLoadLibrary(name) {
                systemLoadLibrary(name + ending) {
                    systemLoad(name) {
                        systemLoad(name + ending) {
                            libraryLoaded = false
                        }
                    }
                }
            }
        }
        return libraryLoaded
    }

}