package io.taiga.client.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.taiga.client.data.auth.AuthRepository
import io.taiga.client.data.preferences.AppearanceMode
import io.taiga.client.data.preferences.AppPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.unifiedpush.android.connector.UnifiedPush
import javax.inject.Inject

enum class SettingsPanel { Root, Appearance, AccentColor, Notifications, Support }

data class SettingsUiState(
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    val accentColorIndex: Int = 0,
    val notifyNewAssigned: Boolean = true,
    val notifyNewInProjects: Boolean = true,
    val notifySound: Boolean = true,
    val localNotificationsEnabled: Boolean = true,
    val unifiedPushEnabled: Boolean = false,
    val unifiedPushDistributorAvailable: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferencesRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = prefs.preferencesFlow.map { p ->
        SettingsUiState(
            appearanceMode = p.appearanceMode,
            accentColorIndex = p.accentColorIndex,
            notifyNewAssigned = p.notifyNewAssigned,
            notifyNewInProjects = p.notifyNewInProjects,
            notifySound = p.notifySound,
            localNotificationsEnabled = p.localNotificationsEnabled,
            unifiedPushEnabled = p.unifiedPushEnabled,
            unifiedPushDistributorAvailable = UnifiedPush.getDistributors(context).isNotEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setAppearanceMode(mode: AppearanceMode) {
        viewModelScope.launch { prefs.setAppearanceMode(mode) }
    }

    fun setAccentColorIndex(index: Int) {
        viewModelScope.launch { prefs.setAccentColorIndex(index) }
    }

    fun setNotifyNewAssigned(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotifyNewAssigned(enabled) }
    }

    fun setNotifyNewInProjects(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotifyNewInProjects(enabled) }
    }

    fun setNotifySound(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotifySound(enabled) }
    }

    fun setLocalNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setLocalNotificationsEnabled(enabled) }
    }

    fun setUnifiedPushEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setUnifiedPushEnabled(enabled) }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
