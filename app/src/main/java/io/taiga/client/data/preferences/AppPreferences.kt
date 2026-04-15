package io.taiga.client.data.preferences

enum class AppearanceMode { SYSTEM, LIGHT, DARK }

data class AppPreferences(
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    val accentColorIndex: Int = 0,
    val notifyNewAssigned: Boolean = true,
    val notifyNewInProjects: Boolean = true,
    val notifySound: Boolean = true,
    val localNotificationsEnabled: Boolean = true,
    val unifiedPushEnabled: Boolean = false,
)
