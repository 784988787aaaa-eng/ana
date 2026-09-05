package com.smartledger.aldaftar.ui.viewmodel.backup

import android.net.Uri

/**
 * ثوابت بروتوكولية لتحليل روابط وتفويض مصادقة OAuth.
 */
private const val SCHEME_HTTP_PREFIX = "http://"
private const val SCHEME_HTTPS_PREFIX = "https://"
private const val QUERY_PARAM_CODE_KEY = "code"
private const val QUERY_PARAM_CODE_EQUALS = "code="
private const val QUERY_PARAM_DELIMITER = "&"

object OAuthCodeParser {
    fun extractCodeFromInput(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        if (trimmed.startsWith(SCHEME_HTTP_PREFIX) || trimmed.startsWith(SCHEME_HTTPS_PREFIX) || trimmed.contains(QUERY_PARAM_CODE_EQUALS)) {
            var extracted = ""
            try {
                val parsedUri = Uri.parse(trimmed)
                extracted = parsedUri.getQueryParameter(QUERY_PARAM_CODE_KEY) ?: ""
            } catch (e: Exception) {}
            if (extracted.isEmpty()) {
                val idx = trimmed.indexOf(QUERY_PARAM_CODE_EQUALS)
                if (idx != -1) {
                    val start = idx + QUERY_PARAM_CODE_EQUALS.length
                    val end = trimmed.indexOf(QUERY_PARAM_DELIMITER, start).let { if (it == -1) trimmed.length else it }
                    extracted = trimmed.substring(start, end)
                }
            }
            return extracted.takeIf { it.isNotEmpty() } ?: trimmed
        }
        return trimmed
    }
}
