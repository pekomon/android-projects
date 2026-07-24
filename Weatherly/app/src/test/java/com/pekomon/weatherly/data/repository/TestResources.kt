package com.pekomon.weatherly.data.repository

internal fun resourceText(path: String): String {
    val stream = Thread.currentThread().contextClassLoader?.getResourceAsStream(path)
        ?: error("Missing test resource: $path")
    return stream.bufferedReader().use { it.readText() }
}
