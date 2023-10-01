package dev.datlag.kcef.common

import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.util.stream.Collectors

internal fun File.unquarantine() = scopeCatching {
    val process = Runtime.getRuntime().exec(arrayOf("xattr", "-r", "-d", "com.apple.quarantine", this.canonicalPath))
    process.waitFor()
}

internal fun File.deleteDir() = scopeCatching {
    if (!this.existsSafely()) {
        return@scopeCatching
    }

    if (this.isDirectorySafely()) {
        this.listFilesSafely().forEach {
            it.deleteSafely()
        }
    }
    this.deleteSafely()
}

internal fun File?.existsSafely(): Boolean {
    if (this == null) {
        return false
    }

    return scopeCatching {
        Files.exists(this.toPath())
    }.getOrNull() ?: scopeCatching {
        this.exists()
    }.getOrNull() ?: false
}

internal fun File.canReadSafely(): Boolean {
    return scopeCatching {
        Files.isReadable(this.toPath())
    }.getOrNull() ?: scopeCatching {
        this.canRead()
    }.getOrNull() ?: false
}

internal fun File.canWriteSafely(): Boolean {
    return scopeCatching {
        Files.isWritable(this.toPath())
    }.getOrNull() ?: scopeCatching {
        this.canWrite()
    }.getOrNull() ?: false
}

internal fun File.deleteSafely(): Boolean {
    return scopeCatching {
        Files.delete(this.toPath())
    }.isSuccess || scopeCatching {
        this.delete()
    }.getOrNull() ?: false
}

internal fun File.isDirectorySafely(): Boolean {
    return scopeCatching {
        Files.isDirectory(this.toPath())
    }.getOrNull() ?: scopeCatching {
        this.isDirectory
    }.getOrNull() ?: false
}

internal fun File.listFilesSafely(): List<File> {
    return scopeCatching {
        this.listFiles()
    }.getOrNull()?.filterNotNull() ?: scopeCatching {
        Files.list(this.toPath()).collect(Collectors.toList()).mapNotNull { path ->
            path?.toFile()
        }
    }.getOrNull() ?: emptyList()
}

internal fun File.mkdirsSafely(): Boolean {
    return scopeCatching {
        Files.createDirectories(this.toPath()).toFile().existsSafely()
    }.getOrNull() ?: scopeCatching {
        this.mkdirs()
    }.getOrNull() ?: false
}

internal fun File.mkdirSafely(): Boolean {
    return scopeCatching {
        Files.createDirectory(this.toPath()).toFile().existsSafely()
    }.getOrNull() ?: scopeCatching {
        this.mkdir()
    }.getOrNull() ?: false
}

internal fun File.createSafely(): Boolean {
    return scopeCatching {
        Files.createFile(this.toPath()).toFile().existsSafely()
    }.getOrNull() ?: scopeCatching {
        this.createNewFile()
    }.getOrNull() ?: false
}

internal fun createTempSafely(prefix: String, suffix: String): File? {
    return scopeCatching {
        Files.createTempFile(
            prefix,
            suffix
        ).toFile()
    }.getOrNull() ?: scopeCatching {
        File.createTempFile(
            prefix,
            suffix
        )
    }.getOrNull()
}

internal fun File.deleteOnExitSafely() {
    scopeCatching {
        this.deleteOnExit()
    }.getOrNull()
}

internal fun File.moveSafely(target: File): File {
    return scopeCatching {
        Files.move(
            this.toPath(),
            target.toPath()
        ).toFile()
    }.getOrNull() ?: scopeCatching {
        if (this.renameTo(target)) {
            target
        } else {
            this
        }
    }.getOrNull() ?: this
}

internal fun File.isSymlinkSafely(): Boolean {
    return scopeCatching {
        Files.isSymbolicLink(this.toPath())
    }.getOrNull() ?: scopeCatching {
        !Files.isRegularFile(this.toPath(), LinkOption.NOFOLLOW_LINKS)
    }.getOrNull() ?: false
}

internal fun File.getRealFile(): File {
    return if (isSymlinkSafely()) scopeCatching {
        Files.readSymbolicLink(this.toPath()).toFile()
    }.getOrNull() ?: this else this
}

internal fun File.isSame(file: File?): Boolean {
    var sourceFile = this.getRealFile()
    if (!sourceFile.existsSafely()) {
        sourceFile = this
    }

    var targetFile = file?.getRealFile() ?: file
    if (!targetFile.existsSafely()) {
        targetFile = file
    }

    return if (targetFile == null) {
        false
    } else {
        this == targetFile || scopeCatching {
            sourceFile.absoluteFile == targetFile.absoluteFile || Files.isSameFile(sourceFile.toPath(), targetFile.toPath())
        }.getOrNull() ?: false
    }
}

internal fun File.parentSafely(): File? {
    return scopeCatching {
        this.toPath().parent?.toFile()
    }.getOrNull() ?: scopeCatching {
        this.parentFile
    }.getOrNull()
}
