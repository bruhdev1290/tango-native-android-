package io.taiga.client.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserStorySummary(
    val id: Long,
    val ref: Int,
    val subject: String,
    val status_extra_info: StatusExtraInfo? = null,
    val assigned_to: Long? = null,
    val assigned_to_extra_info: UserExtraInfo? = null,
    val project: Long,
    val project_extra_info: ProjectExtraInfo? = null,
    val milestone: Long? = null,
    val modified_date: String? = null,
    val created_date: String? = null,
    val is_closed: Boolean = false,
)
