package dev.datlag.kcef.common

import java.io.File
import java.nio.file.Files
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

internal fun File.mkdirsSafely(): Boolean = scopeCatching {
    this.mkdirs()
}.getOrNull() ?: false

internal fun File.createSafely(): Boolean {
    return scopeCatching {
        Files.createFile(this.toPath()).toFile().existsSafely()
    }.getOrNull() ?: scopeCatching {
        this.createNewFile()
    }.getOrNull() ?: false
}
