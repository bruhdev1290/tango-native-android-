package io.taiga.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.taiga.client.data.lock.AppLockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val appLockRepository: AppLockRepository,
) : ViewModel() {

    private val _isLocked = MutableStateFlow(appLockRepository.isLockEnabled())
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun lockApp() {
        if (appLockRepository.isLockEnabled()) {
            _isLocked.value = true
        }
    }

    fun unlockWithPin(pin: String): Boolean {
        return if (appLockRepository.verifyPin(pin)) {
            _isLocked.value = false
            true
        } else {
            false
        }
    }

    fun unlockWithBiometric() {
        _isLocked.value = false
    }

    fun isPinEnabled(): Boolean = appLockRepository.isPinEnabled()

    fun isBiometricEnabled(): Boolean = appLockRepository.isBiometricEnabled()

    fun isBiometricAvailable(): Boolean = appLockRepository.isBiometricAvailable()

    fun setupPin(pin: String) {
        viewModelScope.launch { appLockRepository.setupPin(pin) }
    }

    fun removePin() {
        viewModelScope.launch {
            appLockRepository.removePin()
            _isLocked.value = false
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { appLockRepository.setBiometricEnabled(enabled) }
    }
}
