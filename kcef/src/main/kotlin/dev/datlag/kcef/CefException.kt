package dev.datlag.kcef

internal sealed class CefException(override val message: String) : Exception(message) {
    data object Initialization : CefException("Error while initializing JCef")
    data object Startup : CefException("JCef did not initialize correctly!")
    data class UnsupportedPlatform(
        val os: String?,
        val arch: String?
    ) : CefException("Can not detect your current platform. Is it supported? [$os, $arch]")
}