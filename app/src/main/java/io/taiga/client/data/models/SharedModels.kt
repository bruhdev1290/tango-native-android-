package io.taiga.client.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatusExtraInfo(
    val name: String,
    val color: String? = null,
    val is_closed: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class UserExtraInfo(
    val id: Long,
    val username: String,
    val full_name_display: String,
)

@JsonClass(generateAdapter = true)
data class ProjectExtraInfo(
    val id: Long,
    val name: String,
    val slug: String,
    val logo_small_url: String? = null,
)
