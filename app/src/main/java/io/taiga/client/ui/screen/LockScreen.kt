package io.taiga.client.ui.screen

import android.os.Build
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay

private const val PIN_LENGTH = 6

@Composable
fun LockScreen(
    isBiometricEnabled: Boolean,
    onPinEntered: (String) -> Boolean,
    onBiometricSuccess: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentOnBiometricSuccess by rememberUpdatedState(onBiometricSuccess)

    fun launchBiometric() {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    currentOnBiometricSuccess()
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Taiga")
            .setSubtitle("Use your biometric to unlock")
            .setNegativeButtonText("Use PIN")
            .build()
        prompt.authenticate(info)
    }

    LaunchedEffect(Unit) {
        if (isBiometricEnabled) launchBiometric()
    }

    LaunchedEffect(pin) {
        if (pin.length == PIN_LENGTH) {
            val correct = onPinEntered(pin)
            if (!correct) {
                showError = true
                delay(600)
                pin = ""
                showError = false
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Taiga",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Enter your PIN to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(32.dp))

            PinDots(filled = pin.length, total = PIN_LENGTH, showError = showError)
            Spacer(Modifier.height(8.dp))

            if (showError) {
                Text(
                    text = "Incorrect PIN",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                Spacer(Modifier.height(20.dp))
            }
            Spacer(Modifier.height(24.dp))

            NumberPad(
                onDigit = { digit ->
                    if (pin.length < PIN_LENGTH) pin += digit
                },
                onDelete = {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                },
                biometricEnabled = isBiometricEnabled,
                onBiometric = ::launchBiometric,
            )
        }
    }
}

@Composable
private fun PinDots(filled: Int, total: Int, showError: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(total) { index ->
            val isFilled = index < filled
            val dotColor by animateColorAsState(
                targetValue = when {
                    showError -> MaterialTheme.colorScheme.error
                    isFilled -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outlineVariant
                },
                animationSpec = tween(150),
                label = "dot_color_$index",
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }
    }
}

@Composable
private fun NumberPad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    biometricEnabled: Boolean,
    onBiometric: () -> Unit,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { digit ->
                    PadKey(label = digit, onClick = { onDigit(digit) })
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (biometricEnabled) {
                PadIconKey(
                    icon = { Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", modifier = Modifier.size(28.dp)) },
                    onClick = onBiometric,
                )
            } else {
                Spacer(Modifier.size(72.dp))
            }
            PadKey(label = "0", onClick = { onDigit("0") })
            PadKey(label = "⌫", onClick = onDelete)
        }
    }
}

@Composable
private fun PadKey(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun PadIconKey(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
    ) {
        icon()
    }
}
