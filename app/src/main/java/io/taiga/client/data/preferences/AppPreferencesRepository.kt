package io.taiga.client.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.dataStore

    private object Keys {
        val APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
        val ACCENT_COLOR_INDEX = intPreferencesKey("accent_color_index")
        val NOTIFY_NEW_ASSIGNED = booleanPreferencesKey("notify_new_assigned")
        val NOTIFY_NEW_IN_PROJECTS = booleanPreferencesKey("notify_new_in_projects")
        val NOTIFY_SOUND = booleanPreferencesKey("notify_sound")
        val LOCAL_NOTIFICATIONS_ENABLED = booleanPreferencesKey("local_notifications_enabled")
        val UNIFIED_PUSH_ENABLED = booleanPreferencesKey("unified_push_enabled")
    }

    val preferencesFlow: Flow<AppPreferences> = dataStore.data.map { prefs ->
        AppPreferences(
            appearanceMode = prefs[Keys.APPEARANCE_MODE]
                ?.let { runCatching { AppearanceMode.valueOf(it) }.getOrNull() }
                ?: AppearanceMode.SYSTEM,
            accentColorIndex = prefs[Keys.ACCENT_COLOR_INDEX] ?: 0,
            notifyNewAssigned = prefs[Keys.NOTIFY_NEW_ASSIGNED] ?: true,
            notifyNewInProjects = prefs[Keys.NOTIFY_NEW_IN_PROJECTS] ?: true,
            notifySound = prefs[Keys.NOTIFY_SOUND] ?: true,
            localNotificationsEnabled = prefs[Keys.LOCAL_NOTIFICATIONS_ENABLED] ?: true,
            unifiedPushEnabled = prefs[Keys.UNIFIED_PUSH_ENABLED] ?: false,
        )
    }

    suspend fun setAppearanceMode(mode: AppearanceMode) {
        dataStore.edit { it[Keys.APPEARANCE_MODE] = mode.name }
    }

    suspend fun setAccentColorIndex(index: Int) {
        dataStore.edit { it[Keys.ACCENT_COLOR_INDEX] = index }
    }

    suspend fun setNotifyNewAssigned(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFY_NEW_ASSIGNED] = enabled }
    }

    suspend fun setNotifyNewInProjects(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFY_NEW_IN_PROJECTS] = enabled }
    }

    suspend fun setNotifySound(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFY_SOUND] = enabled }
    }

    suspend fun setLocalNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.LOCAL_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setUnifiedPushEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.UNIFIED_PUSH_ENABLED] = enabled }
    }
}
