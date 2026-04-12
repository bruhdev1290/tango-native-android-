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

    // Rewrite tree.taiga.io -> api.taiga.io (web URL to API URL)
    val withApiHost = withScheme.replace("//tree.taiga.io", "//api.taiga.io")

    val withoutTrailingSlash = withApiHost.trimEnd('/')
    return if (withoutTrailingSlash.endsWith("/api/v1")) {
        "$withoutTrailingSlash/"
    } else {
        "$withoutTrailingSlash/api/v1/"
    }
}
