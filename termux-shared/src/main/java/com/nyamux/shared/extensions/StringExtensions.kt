package com.nyamux.shared.extensions

import com.nyamux.shared.file.FileUtils

fun String.canonicalPath(prefix: String?): String = FileUtils.getCanonicalPath(this, prefix)
fun String.normalizedPath(): String? = FileUtils.normalizePath(this)
fun String.sanitizedFileName(sanitizeWs: Boolean = false, toLower: Boolean = false): String? =
    FileUtils.sanitizeFileName(this, sanitizeWs, toLower)
fun String.isInDir(dirPath: String, ensureUnder: Boolean = true): Boolean =
    FileUtils.isPathInDirPath(this, dirPath, ensureUnder)
