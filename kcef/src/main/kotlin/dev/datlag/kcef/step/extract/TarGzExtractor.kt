package dev.datlag.kcef.step.extract

import dev.datlag.kcef.CefException
import dev.datlag.kcef.Platform
import dev.datlag.kcef.common.*
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


internal data object TarGzExtractor : Extractor {

    override fun extract(installDir: File, downloadedFile: File, bufferSize: Long) {
        downloadedFile.inputStream().use { `in` ->
            GzipCompressorInputStream(`in`).use { gzipIn ->
                TarArchiveInputStream(gzipIn).use { tarIn ->
                    var entry: TarArchiveEntry? = null

                    while ((tarIn.nextTarEntry).also { entry = it } != null) {
                        val currentEntry = entry

                        if (currentEntry != null) {
                            val file = File(installDir, currentEntry.name)
                            if (!file.validate(installDir)) {
                                throw CefException.BadArchive
                            }

                            if (currentEntry.isDirectory) {
                                file.mkdirSafely()
                                file.setExecutable(true, false)
                            } else {
                                var count: Int
                                val data = ByteArray(bufferSize.toInt())
                                BufferedOutputStream(
                                    FileOutputStream(file, false), bufferSize.toInt()
                                ).use { dest ->
                                    while (tarIn.read(data, 0, bufferSize.toInt()).also { count = it } != -1) {
                                        dest.write(data, 0, count)
                                    }
                                }
                                file.setExecutable(true, false)
                            }
                        }
                    }
                }
            }
        }
        downloadedFile.deleteSafely()
    }

    fun move(installDir: File) {
        val os = Platform.getCurrentPlatform().os
        when {
            os.isLinux-> linuxMove(installDir)
            os.isMacOSX -> macMove(installDir)
            os.isWindows -> winMove(installDir)
            else -> linuxMove(installDir)
        }
    }

    private fun linuxMove(installDir: File) {
        var foundDir: File? = null
        var foundParent: File? = null

        installDir.listFilesSafely().forEach { parent ->
            if (File(parent, "lib").existsSafely()) {
                foundDir = File(parent, "lib")
                foundParent = parent
            }
        }

        foundDir?.let {
            val target = it.moveSafely(File(installDir, "lib"))
            foundParent?.let { p ->
                p.deleteDir()
                p.deleteSafely()
                p.deleteOnExitSafely()
            }

            installDir.listFilesSafely().forEach { deleteCandidate ->
                if (!deleteCandidate.isSame(target)) {
                    deleteCandidate.deleteSafely()
                }
            }

            target.listFilesSafely().forEach { moveCandidate ->
                moveCandidate.moveSafely(File(installDir, moveCandidate.name))
            }

            target.deleteSafely()
        }
    }

    private fun macMove(installDir: File) {
        var foundDir: File? = null
        var foundParent: File? = null

        installDir.listFilesSafely().forEach { parent ->
            if (File(parent, "Contents").existsSafely()) {
                foundDir = File(parent, "Contents")
                foundParent = parent
            }
        }

        val target = File(installDir, "lib").also { it.mkdirSafely() }
        foundDir?.let { contents ->
            File(contents, "Home/lib").listFilesSafely().forEach { moveCandidate ->
                moveCandidate.moveSafely(File(target, moveCandidate.name))
            }

            File(contents, "Frameworks/Chromium Embedded Framework.framework").moveSafely(
                File(target, "Chromium Embedded Framework.framework")
            )

            File(contents, "Frameworks/jcef Helper.app").moveSafely(
                File(target, "jcef Helper.app")
            )

            foundParent?.let { p ->
                p.deleteDir()
                p.deleteSafely()
                p.deleteOnExitSafely()
            }

            installDir.listFilesSafely().forEach { deleteCandidate ->
                if (!deleteCandidate.isSame(target)) {
                    deleteCandidate.deleteSafely()
                }
            }

            target.listFilesSafely().forEach { moveCandidate ->
                moveCandidate.moveSafely(File(installDir, moveCandidate.name))
            }

            target.deleteSafely()
        }
    }

    private fun winMove(installDir: File) {
        var foundDir: File? = null

        installDir.listFilesSafely().forEach { parent ->
            if (File(parent, "lib").existsSafely()) {
                foundDir = parent
            }
        }

        foundDir?.let {
            val target = File(it, "lib").moveSafely(File(installDir, "lib"))
            File(it, "bin").listFilesSafely().forEach { moveCandidate ->
                moveCandidate.moveSafely(File(target, moveCandidate.name))
            }

            installDir.listFilesSafely().forEach { deleteCandidate ->
                if (!deleteCandidate.isSame(target)) {
                    deleteCandidate.deleteSafely()
                }
            }

            target.listFilesSafely().forEach { moveCandidate ->
                moveCandidate.moveSafely(File(installDir, moveCandidate.name))
            }

            target.deleteSafely()
        }
    }
}