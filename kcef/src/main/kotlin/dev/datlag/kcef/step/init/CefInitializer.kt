package dev.datlag.kcef.step.init

import dev.datlag.kcef.KCEFException
import dev.datlag.kcef.Platform
import dev.datlag.kcef.common.*
import dev.datlag.kcef.common.scopeCatching
import dev.datlag.kcef.common.systemAddPath
import dev.datlag.kcef.common.systemLoad
import dev.datlag.kcef.common.systemLoadLibrary
import dev.datlag.kcef.common.systemProperty
import org.cef.CefApp
import org.cef.CefSettings
import org.cef.SystemBootstrap
import java.io.File

internal data object CefInitializer {

    fun initialize(installDir: File, cefArgs: Collection<String>, cefSettings: CefSettings): CefApp {
        val currentOs = Platform.getCurrentPlatform().os

        if (currentOs.isMacOSX) {
            systemAddPath(installDir)
            (currentOs as Platform.OS.MACOSX).let { mac ->
                systemAddPath(mac.getFrameworkPath(installDir))
                systemAddPath(mac.getMainBundlePath(installDir))
                systemAddPath(mac.getBrowserPath(installDir))

                systemProperty("java.home", mac.getFrameworkPath(installDir))
            }
        }

        loadLibrary(installDir, "jawt")

        if(cefArgs.none { it.trim().equals("--disable-gpu", true) }) {
            // GPU required
            loadLibrary(installDir, "EGL")
            loadLibrary(installDir, "GLESv2")
        }

        SystemBootstrap.setLoader {
            if (!loadLibrary(installDir, it)) {
                println("Could not load '$it' library")
            }
        }

        if (cefSettings.locales_dir_path.isNullOrEmpty()) {
            cefSettings.locales_dir_path = File(installDir, "locales").canonicalPath
        }

        if (cefSettings.resources_dir_path.isNullOrEmpty()) {
            cefSettings.resources_dir_path = currentOs.getResourcesPath(installDir)
        }

        if (cefSettings.browser_subprocess_path.isNullOrEmpty()) {
            cefSettings.browser_subprocess_path = currentOs.getBrowserPath(installDir)
        }

        val success = if (currentOs.isMacOSX) {
            val macOs = currentOs as Platform.OS.MACOSX

            CefApp.startup(macOs.getFixedArgs(installDir, cefArgs).toTypedArray())
        } else {
            CefApp.startup(cefArgs.toTypedArray())
        }

        if (!success) {
            throw KCEFException.Startup
        }
        loadLibrary(installDir, "libcef")
        return CefApp.getInstanceIfAny() ?: scopeCatching {
            CefApp.getInstance(cefSettings)
        }.getOrNull() ?: CefApp.getInstance()
    }

    /**
     * The method required for loading a library may differ on platforms.
     * This loading process may be a bit overkill but this way we can make sure the required libraries are loaded.
     */
    private fun loadLibrary(installDir: File, name: String): Boolean {
        val os = Platform.getCurrentPlatform().os
        val ending = when {
            os.isWindows -> ".dll"
            os.isLinux -> ".so"
            os.isMacOSX -> ".dylib"
            else -> ""
        }

        var installDirLoaded = true
        systemLoad(File(installDir, name)) {
            systemLoad(File(installDir, name + ending)) {
                systemLoad(File(installDir, "lib$name")) {
                    systemLoad(File(installDir, "lib$name$ending")) {
                        installDirLoaded = false
                    }
                }
            }
        }
        if (installDirLoaded) {
            return true
        }

        var installDirLibraryLoaded = true
        systemLoadLibrary(File(installDir, name)) {
            systemLoadLibrary(File(installDir, name + ending)) {
                systemLoadLibrary(File(installDir, "lib$name")) {
                    systemLoadLibrary(File(installDir, "lib$name$ending")) {
                        installDirLibraryLoaded = false
                    }
                }
            }
        }

        if (installDirLibraryLoaded) {
            return true
        }

        var libraryLoaded = true
        systemLoad(name) {
            systemLoad(name + ending) {
                systemLoad("lib$name") {
                    systemLoad("lib$name$ending") {
                        systemLoadLibrary(name) {
                            systemLoadLibrary(name + ending) {
                                systemLoadLibrary("lib$name") {
                                    systemLoadLibrary("lib$name$ending") {
                                        libraryLoaded = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return libraryLoaded
    }

}