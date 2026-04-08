package io.taiga.client.data.models

data class IssueDetail(
    val id: Long,
    val ref: Long,
    val subject: String,
    val description: String? = null,
    val description_html: String? = null,
    val created_date: String? = null,
    val modified_date: String? = null,
    val finished_date: String? = null,
    val due_date: String? = null,
    val due_date_reason: String? = null,
    val is_blocked: Boolean? = null,
    val blocked_note: String? = null,
    val project: ProjectSummary? = null,
    val status: IssueStatusSummary? = null,
    val type: IssueTypeSummary? = null,
    val priority: IssuePrioritySummary? = null,
    val severity: IssueSeveritySummary? = null,
    val owner: UserSummary? = null,
    val assigned_to: UserSummary? = null,
)

data class IssueStatusSummary(
    val id: Long,
    val name: String? = null,
    val slug: String? = null,
    val color: String? = null,
    val is_closed: Boolean? = null,
)

data class IssueTypeSummary(
    val id: Long,
    val name: String? = null,
    val color: String? = null,
)

data class IssuePrioritySummary(
    val id: Long,
    val name: String? = null,
    val color: String? = null,
)

data class IssueSeveritySummary(
    val id: Long,
    val name: String? = null,
    val color: String? = null,
)

data class UserSummary(
    val id: Long,
    val username: String? = null,
    val full_name_display: String? = null,
)
