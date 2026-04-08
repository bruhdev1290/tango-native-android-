package io.taiga.client.data.models

data class IssueSummary(
    val id: Long,
    val ref: Long,
    val subject: String,
    val description: String? = null,
    val project: Long? = null,
)
