package dev.datlag.kcef.common

import java.io.File

internal fun systemProperty(key: String): String? = scopeCatching {
    System.getProperty(key).ifEmpty {
        null
    }
}.getOrNull()

internal fun systemEnv(key: String): String? = scopeCatching {
    System.getenv(key).ifEmpty {
        null
    }
}.getOrNull()

internal fun systemProperty(key: String, value: String): String? = scopeCatching {
    System.setProperty(key, value).ifEmpty {
        null
    }
}.getOrNull()

internal fun systemLoadLibrary(value: String, onError: () -> Unit = { }) {
    if (scopeCatching {
        System.loadLibrary(value)
    }.isFailure) {
        onError()
    }
}

internal fun systemLoadLibrary(value: File, onError: () -> Unit = { }) = systemLoadLibrary(value.canonicalPath, onError)

internal fun systemLoad(value: String, onError: () -> Unit = { }) {
    if (scopeCatching {
        System.load(value)
    }.isFailure) {
        onError()
    }
}

internal fun systemLoad(value: File, onError: () -> Unit = { }) = systemLoad(value.canonicalPath, onError)

internal fun systemAddPath(newPath: String) = scopeCatching {
    val prop = "java.library.path"
    val filePath = scopeCatching {
        File(newPath)
    }.getOrNull()?.run {
        if (isDirectorySafely()) {
            canonicalPath
        } else {
            parentSafely()?.canonicalPath
        }
    }
    var path = systemProperty(prop)
    if (path != null && filePath != null) {
        path = filePath + File.pathSeparator + path
        systemProperty(prop, path)
    }
}

internal fun systemAddPath(file: File) = systemAddPath(file.canonicalPath)

internal fun homeDirectory(): File? {
    return systemProperty("user.home")?.let {
        File(it)
    } ?: systemEnv("HOME")?.let {
        File(it)
    } ?: systemEnv("\$HOME")?.let {
        File(it)
    }
}
