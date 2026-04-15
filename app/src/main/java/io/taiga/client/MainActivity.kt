package io.taiga.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.taiga.client.push.PushRegistrationManager
import io.taiga.client.ui.AppRoot
import io.taiga.client.ui.theme.TaigaClientTheme
import io.taiga.client.ui.viewmodel.SettingsViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var pushRegistrationManager: PushRegistrationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(settingsState.unifiedPushEnabled) {
                if (settingsState.unifiedPushEnabled) {
                    pushRegistrationManager.register()
                } else {
                    pushRegistrationManager.unregister()
                }
            }

            TaigaClientTheme(
                appearanceMode = settingsState.appearanceMode,
                accentColorIndex = settingsState.accentColorIndex,
            ) {
                Surface {
                    AppRoot()
                }
            }
        }
    }
}
