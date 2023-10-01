package dev.datlag.kcef

import dev.datlag.kcef.common.systemProperty
import java.util.*

internal data object Platform {

    private const val PROPERTY_OS_NAME = "os.name"
    private const val PROPERTY_OS_ARCH = "os.arch"

    private var osInfo: OSInfo? = null

    fun getCurrentPlatform(): OSInfo {
        osInfo?.let {
            return it
        }

        val osName = systemProperty(PROPERTY_OS_NAME)
        val osArch = systemProperty(PROPERTY_OS_ARCH)

        val os = osName?.let { OS.matching(it) }
        val arch = osArch?.let { ARCH.matching(it) }

        if (os == null || arch == null) {
            throw CefException.UnsupportedPlatform(osName, osArch)
        } else {
            return OSInfo(os, arch).also {
                osInfo = it
            }
        }
    }

    internal sealed class OS(private vararg val values: String) {
        data object MACOSX : OS("mac", "darwin")
        data object LINUX : OS("nux", "linux")
        data object WINDOWS : OS("win", "windows")

        fun matches(osName: String): Boolean {
            return this.values.contains(osName.lowercase(Locale.ENGLISH))
        }

        val isMacOSX: Boolean
            get() = this is MACOSX || this == MACOSX

        val isLinux: Boolean
            get() = this is LINUX || this == LINUX

        val isWindows: Boolean
            get() = this is WINDOWS || this == WINDOWS

        companion object {
            fun matching(osName: String): OS? = when {
                MACOSX.matches(osName) -> MACOSX
                LINUX.matches(osName) -> LINUX
                WINDOWS.matches(osName) -> WINDOWS
                else -> null
            }
        }
    }

    internal sealed class ARCH(private vararg val values: String) {
        data object AMD64 : ARCH("amd64", "x86_64")
        data object I386 : ARCH("x86", "i386", "i486", "i586", "i686", "i786")
        data object ARM64 : ARCH("arm64", "aarch64")
        data object ARM : ARCH("arm")

        fun matches(osArch: String): Boolean {
            return this.values.contains(osArch.lowercase(Locale.ENGLISH))
        }

        val isAMD64: Boolean
            get() = this is AMD64 || this == AMD64

        val isI386: Boolean
            get() = this is I386 || this == I386

        val isARM64: Boolean
            get() = this is ARM64 || this == ARM64

        val isARM: Boolean
            get() = this is ARM || this == ARM

        companion object {
            fun matching(osArch: String): ARCH? = when {
                AMD64.matches(osArch) -> AMD64
                I386.matches(osArch) -> I386
                ARM64.matches(osArch) -> ARM64
                ARM.matches(osArch) -> ARM
                else -> null
            }
        }
    }

    internal data class OSInfo(
        val os: OS,
        val arch: ARCH
    )

}