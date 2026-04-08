package io.taiga.client.data.models

data class ProjectSummary(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String? = null,
    val is_private: Boolean? = null,
    val logo_small_url: String? = null,
)
