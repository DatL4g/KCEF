package dev.datlag.kcef.step.fetch

import dev.datlag.kcef.CefException
import dev.datlag.kcef.KCEFBuilder
import dev.datlag.kcef.Platform
import dev.datlag.kcef.common.createTempSafely
import dev.datlag.kcef.common.deleteOnExitSafely
import dev.datlag.kcef.model.GitHubRelease
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import java.io.File

internal data object PackageDownloader {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val ContentType_GitHub_Json = ContentType("application", "vnd.github+json")
    private val urlRegex = "(https?://|www.)[-a-zA-Z0-9+&@#/%?=~_|!:.;]*[-a-zA-Z0-9+&@#/%=~_|]".toRegex()

    suspend fun downloadPackage(
        tag: String?,
        progress: KCEFBuilder.InitProgress,
        bufferSize: Long
    ): File {
        val client = HttpClient(OkHttp) {
            followRedirects = true
            install(ContentNegotiation) {
                json(json)
                json(json, ContentType_GitHub_Json)
            }
        }

        val releasesUrl = if (tag.isNullOrEmpty()) {
            "https://api.github.com/repos/JetBrains/JetBrainsRuntime/releases/latest"
        } else {
            "https://api.github.com/repos/JetBrains/JetBrainsRuntime/releases/tags/$tag"
        }

        val gitHubRelease: GitHubRelease = client.get {
            url(releasesUrl)
            accept(ContentType_GitHub_Json)
        }.body()

        val packageUrlList = urlRegex.findAll(gitHubRelease.body).toList().map { it.value }.filterNot {
            it.isBlank() || it.endsWith(".checksum", true)
        }.filter {
            it.contains("jcef", true)
        }

        val platform = Platform.getCurrentPlatform()
        val osPackageList = packageUrlList.filter { url ->
            platform.os.values.any { os ->
                url.contains(os, true)
            }
        }
        val platformPackageList = osPackageList.filter { url ->
            platform.arch.values.any { arch ->
                url.contains(arch, true)
            }
        }

        if (platformPackageList.isEmpty()) {
            client.close()
            throw CefException.UnsupportedPlatformPackage(
                platform.os.toString(),
                platform.arch.toString()
            )
        }

        val sortedPackageList = platformPackageList.sortedWith(compareBy<String> {
            if (it.contains("sdk", true)) {
                1
            } else {
                0
            }
        }.thenBy {
            if (it.endsWith(".tar.gz", true)) {
                0
            } else {
                1
            }
        })

        val file = createTempSafely(
            prefix = "jcef",
            suffix = ".tar.gz"
        ) ?: run {
            client.close()
            throw CefException.DownloadTempFile
        }

        file.deleteOnExitSafely()

        val success = client.prepareGet(sortedPackageList.first().also {
            println("Used package: $it")
        }) {
            onDownload { bytesSentTotal, contentLength ->
                progress.downloading((bytesSentTotal.toFloat() / contentLength.toFloat()) * 100F)
            }
        }.execute { httpResponse ->
            val channel: ByteReadChannel = httpResponse.bodyAsChannel()

            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(bufferSize)
                while (packet.isNotEmpty) {
                    val bytes = packet.readBytes()
                    file.appendBytes(bytes)
                }
            }
            httpResponse.status.isSuccess()
        }

        client.close()
        if (!success) {
            throw CefException.Download
        }
        return file
    }
}