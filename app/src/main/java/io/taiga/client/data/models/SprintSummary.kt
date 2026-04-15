package io.taiga.client.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SprintSummary(
    val id: Long,
    val name: String,
    val project: Long,
    val closed: Boolean = false,
    val estimated_start: String? = null,
    val estimated_finish: String? = null,
)
