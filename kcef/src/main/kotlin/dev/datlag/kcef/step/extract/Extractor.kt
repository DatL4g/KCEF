package dev.datlag.kcef.step.extract

import java.io.File

internal interface Extractor {

    fun extract(installDir: File, downloadedFile: File, bufferSize: Long)
}