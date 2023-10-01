package dev.datlag.kcef.step.fetch

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import java.io.File

internal data object PackageDownloader {

    suspend fun downloadPackage(tag: String?, installDir: File) {
        val client = HttpClient(OkHttp) {
            followRedirects = true
        }

        println("TODO: Implement download")

        client.close()
    }
}