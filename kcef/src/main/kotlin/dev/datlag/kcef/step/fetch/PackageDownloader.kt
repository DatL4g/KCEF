package dev.datlag.kcef.step.fetch

import dev.datlag.kcef.KCEFBuilder
import dev.datlag.kcef.KCEFException
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

    suspend fun downloadPackage(
        download: KCEFBuilder.Download,
        progress: KCEFBuilder.InitProgress
    ): File {

        val downloadUrl = download.transform?.transform(
            client = download.client,
            response = download.client.get(download.url)
        ) ?: download.url

        val file = createTempSafely(
            prefix = "jcef",
            suffix = ".tar.gz"
        ) ?: run {
            download.client.close()
            throw KCEFException.DownloadTempFile
        }

        file.deleteOnExitSafely()

        val buffer = if (download.bufferSize <= 0) {
            16 * 1024
        } else {
            download.bufferSize
        }

        val success = download.client.prepareGet(downloadUrl) {
            onDownload { bytesSentTotal, contentLength ->
                progress.downloading((bytesSentTotal.toFloat() / contentLength.toFloat()) * 100F)
            }
        }.execute { httpResponse ->
            val channel: ByteReadChannel = httpResponse.bodyAsChannel()

            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(buffer)
                while (packet.isNotEmpty) {
                    val bytes = packet.readBytes()
                    file.appendBytes(bytes)
                }
            }
            httpResponse.status.isSuccess()
        }

        download.client.close()
        if (!success) {
            throw KCEFException.Download
        }
        return file
    }
}