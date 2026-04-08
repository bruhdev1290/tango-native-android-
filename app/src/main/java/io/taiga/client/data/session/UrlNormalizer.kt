package io.taiga.client.data.session

private const val DEFAULT_BASE_URL = "https://api.taiga.io/api/v1/"

fun normalizeTaigaApiBaseUrl(value: String): String {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) {
        return DEFAULT_BASE_URL
    }

    val withScheme = when {
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        else -> "https://$trimmed"
    }

    val withoutTrailingSlash = withScheme.trimEnd('/')
    return if (withoutTrailingSlash.endsWith("/api/v1")) {
        "$withoutTrailingSlash/"
    } else {
        "$withoutTrailingSlash/api/v1/"
    }
}
