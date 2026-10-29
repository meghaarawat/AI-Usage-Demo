package com.kitlabs.aiusagedemo

import java.net.URI

object UrlValidator {

    private val ALLOWED_SCHEMES = setOf("http", "https")

    fun isValid(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return try {
            val uri = URI(url.trim())
            uri.scheme?.lowercase() in ALLOWED_SCHEMES
                    && !uri.host.isNullOrBlank()
                    && uri.host.contains('.')
        } catch (_: Exception) {
            false
        }
    }

    fun validate(url: String?): Result<String> {
        val trimmed = url?.trim()
        if (trimmed.isNullOrBlank()) return Result.failure(IllegalArgumentException("URL must not be blank"))

        val uri = runCatching { URI(trimmed) }.getOrElse {
            return Result.failure(IllegalArgumentException("Malformed URL: ${it.message}"))
        }

        if (uri.scheme?.lowercase() !in ALLOWED_SCHEMES)
            return Result.failure(IllegalArgumentException("Unsupported scheme '${uri.scheme}'. Expected: $ALLOWED_SCHEMES"))

        if (uri.host.isNullOrBlank() || !uri.host.contains('.'))
            return Result.failure(IllegalArgumentException("Invalid or missing host in URL"))

        return Result.success(trimmed)
    }
}