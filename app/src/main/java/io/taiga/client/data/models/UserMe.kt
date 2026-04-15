package io.taiga.client.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserMe(
    val id: Long,
    val username: String,
    val full_name_display: String,
    val email: String,
)
