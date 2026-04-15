package io.taiga.client.data.models

enum class ItemKind { STORY, TASK, ISSUE }
enum class ItemScope { ASSIGNED, PROJECT }

enum class ActivityType {
    ASSIGNED,
    NEW_ITEM,
    CHANGED,
    PROJECT,
    NOTIFICATION,
    USER_ADDED,
    PING,
}

data class ActivityItem(
    val id: Long,
    val ref: Long,
    val subject: String,
    val projectId: Long,
    val projectName: String,
    val projectLogoUrl: String?,
    val kind: ItemKind,
    val scope: ItemScope,
    val assignedToId: Long?,
    val modifiedDate: String?,
    val createdDate: String?,
    val activityType: ActivityType = ActivityType.PROJECT,
    val description: String? = null,
)
