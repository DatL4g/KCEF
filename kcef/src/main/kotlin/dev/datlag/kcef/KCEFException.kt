package dev.datlag.kcef

sealed class KCEFException(override val message: String) : Exception(message) {
    data object Startup : KCEFException("JCef did not initialize correctly!")
    data class UnsupportedPlatform(
        val os: String?,
        val arch: String?
    ) : KCEFException("Can not detect your current platform. Is it supported? [$os, $arch]")

    data object InstallationDirectory : KCEFException("Could not create installation directory")
    data object InstallationLock : KCEFException("Could not create install.lock to complete installation")

    data class UnsupportedPlatformPackage(
        val os: String?,
        val arch: String?
    ) : KCEFException("No available package found for your platform [$os, $arch]")

    data object DownloadTempFile : KCEFException("Could not create temp file to download jcef package")

    data object Download : KCEFException("Could not download jcef package")

    data object BadArchive : KCEFException("The provided archive contains a bad (malicious) file")

    data object NotInitialized : KCEFException("Cef was not initialized.")

    data object Disposed : KCEFException("Cef is disposed.")

    data object ApplicationRestartRequired : KCEFException("Application needs to restart.")

    data class Error(override val cause: Throwable?) : KCEFException("Got error: ${cause?.message}")
}