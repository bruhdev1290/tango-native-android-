package io.taiga.client.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.taiga.client.data.preferences.AppearanceMode
import io.taiga.client.ui.theme.accentPresets
import androidx.core.content.ContextCompat
import io.taiga.client.ui.viewmodel.SettingsPanel
import io.taiga.client.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationPermissionGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
    var currentPanel by remember { mutableStateOf(SettingsPanel.Root) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                    onDismiss()
                }) { Text("Log Out", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentPanel) {
                            SettingsPanel.Root -> "Settings"
                            SettingsPanel.Appearance -> "Appearance"
                            SettingsPanel.AccentColor -> "Accent Color"
                            SettingsPanel.Notifications -> "Notifications"
                            SettingsPanel.Support -> "Support"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentPanel == SettingsPanel.Root) onDismiss()
                        else currentPanel = SettingsPanel.Root
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { innerPadding ->
        when (currentPanel) {
            SettingsPanel.Root -> SettingsRootPanel(
                modifier = Modifier.padding(innerPadding),
                onNavigateTo = { currentPanel = it },
                onLogout = { showLogoutDialog = true },
                onOpenGitHub = { /* handled below */ },
            )
            SettingsPanel.Appearance -> AppearancePanel(
                modifier = Modifier.padding(innerPadding),
                selected = state.appearanceMode,
                onSelect = viewModel::setAppearanceMode,
            )
            SettingsPanel.AccentColor -> AccentColorPanel(
                modifier = Modifier.padding(innerPadding),
                selectedIndex = state.accentColorIndex,
                onSelect = viewModel::setAccentColorIndex,
            )
            SettingsPanel.Notifications -> NotificationsPanel(
                modifier = Modifier.padding(innerPadding),
                notifyNewAssigned = state.notifyNewAssigned,
                notifyNewInProjects = state.notifyNewInProjects,
                notifySound = state.notifySound,
                localNotificationsEnabled = state.localNotificationsEnabled,
                unifiedPushEnabled = state.unifiedPushEnabled,
                unifiedPushDistributorAvailable = state.unifiedPushDistributorAvailable,
                permissionGranted = notificationPermissionGranted,
                onNewAssignedChange = viewModel::setNotifyNewAssigned,
                onNewInProjectsChange = viewModel::setNotifyNewInProjects,
                onSoundChange = viewModel::setNotifySound,
                onLocalNotificationsChange = viewModel::setLocalNotificationsEnabled,
                onUnifiedPushChange = viewModel::setUnifiedPushEnabled,
            )
            SettingsPanel.Support -> SupportPanel(modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun SettingsRootPanel(
    modifier: Modifier = Modifier,
    onNavigateTo: (SettingsPanel) -> Unit,
    onLogout: () -> Unit,
    onOpenGitHub: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsSectionHeader("Preferences")
        ListItem(
            headlineContent = { Text("Appearance") },
            trailingContent = { Icon(Icons.Default.KeyboardArrowRight, null) },
            modifier = Modifier.clickable { onNavigateTo(SettingsPanel.Appearance) },
        )
        Divider(modifier = Modifier.padding(start = 16.dp))
        ListItem(
            headlineContent = { Text("Accent Color") },
            trailingContent = { Icon(Icons.Default.KeyboardArrowRight, null) },
            modifier = Modifier.clickable { onNavigateTo(SettingsPanel.AccentColor) },
        )
        Divider(modifier = Modifier.padding(start = 16.dp))
        ListItem(
            headlineContent = { Text("Notifications") },
            trailingContent = { Icon(Icons.Default.KeyboardArrowRight, null) },
            modifier = Modifier.clickable { onNavigateTo(SettingsPanel.Notifications) },
        )

        Spacer(Modifier.height(16.dp))
        SettingsSectionHeader("Help")
        ListItem(
            headlineContent = { Text("Support") },
            trailingContent = { Icon(Icons.Default.KeyboardArrowRight, null) },
            modifier = Modifier.clickable { onNavigateTo(SettingsPanel.Support) },
        )
        Divider(modifier = Modifier.padding(start = 16.dp))
        ListItem(
            headlineContent = { Text("GitHub") },
            trailingContent = { Icon(Icons.Default.KeyboardArrowRight, null) },
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/taigaio/taiga-mobile"))
                context.startActivity(intent)
            },
        )

        Spacer(Modifier.height(16.dp))
        SettingsSectionHeader("Account")
        ListItem(
            headlineContent = {
                Text("Log Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            },
            modifier = Modifier.clickable { onLogout() },
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun AppearancePanel(
    modifier: Modifier = Modifier,
    selected: AppearanceMode,
    onSelect: (AppearanceMode) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        AppearanceMode.entries.forEach { mode ->
            val label = when (mode) {
                AppearanceMode.SYSTEM -> "System Default"
                AppearanceMode.LIGHT -> "Light"
                AppearanceMode.DARK -> "Dark"
            }
            ListItem(
                headlineContent = { Text(label) },
                trailingContent = {
                    RadioButton(selected = selected == mode, onClick = { onSelect(mode) })
                },
                modifier = Modifier.clickable { onSelect(mode) },
            )
            Divider(modifier = Modifier.padding(start = 16.dp))
        }
    }
}

@Composable
private fun AccentColorPanel(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(accentPresets) { index, color ->
            val isSelected = index == selectedIndex
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                        else Modifier
                    )
                    .clickable { onSelect(index) },
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsPanel(
    modifier: Modifier = Modifier,
    notifyNewAssigned: Boolean,
    notifyNewInProjects: Boolean,
    notifySound: Boolean,
    localNotificationsEnabled: Boolean,
    unifiedPushEnabled: Boolean,
    unifiedPushDistributorAvailable: Boolean,
    permissionGranted: Boolean,
    onNewAssignedChange: (Boolean) -> Unit,
    onNewInProjectsChange: (Boolean) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onLocalNotificationsChange: (Boolean) -> Unit,
    onUnifiedPushChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxSize()) {
        if (!permissionGranted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Notifications are disabled",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = {
                    val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                }) { Text("Open Settings") }
            }
        }
        ListItem(
            headlineContent = { Text("New Assigned Items") },
            trailingContent = {
                Switch(checked = notifyNewAssigned, onCheckedChange = onNewAssignedChange)
            },
            modifier = Modifier.clickable { onNewAssignedChange(!notifyNewAssigned) },
        )
        Divider(modifier = Modifier.padding(start = 16.dp))
        ListItem(
            headlineContent = { Text("New Items in Projects") },
            trailingContent = {
                Switch(checked = notifyNewInProjects, onCheckedChange = onNewInProjectsChange)
            },
            modifier = Modifier.clickable { onNewInProjectsChange(!notifyNewInProjects) },
        )
        Divider(modifier = Modifier.padding(start = 16.dp))
        ListItem(
            headlineContent = { Text("Sound") },
            trailingContent = {
                Switch(checked = notifySound, onCheckedChange = onSoundChange)
            },
            modifier = Modifier.clickable { onSoundChange(!notifySound) },
        )
        Divider(modifier = Modifier.padding(start = 16.dp))
        ListItem(
            headlineContent = { Text("On-Device Notifications") },
            supportingContent = { Text("Show notifications for activity in the app") },
            trailingContent = {
                Switch(checked = localNotificationsEnabled, onCheckedChange = onLocalNotificationsChange)
            },
            modifier = Modifier.clickable { onLocalNotificationsChange(!localNotificationsEnabled) },
        )
        Divider(modifier = Modifier.padding(start = 16.dp))
        if (unifiedPushEnabled && !unifiedPushDistributorAvailable) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "No UnifiedPush distributor installed",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://unifiedpush.org/users/distributors/"))
                    context.startActivity(intent)
                }) { Text("Learn more") }
            }
        }
        ListItem(
            headlineContent = { Text("UnifiedPush") },
            supportingContent = { Text("Receive push notifications via UnifiedPush") },
            trailingContent = {
                Switch(checked = unifiedPushEnabled, onCheckedChange = onUnifiedPushChange)
            },
            modifier = Modifier.clickable { onUnifiedPushChange(!unifiedPushEnabled) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupportPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val topics = listOf("Bug Report", "Feature Request", "General Question", "Other")
    var selectedTopic by remember { mutableStateOf(topics[0]) }
    var topicExpanded by remember { mutableStateOf(false) }
    var details by remember { mutableStateOf("") }
    var includeLogs by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ExposedDropdownMenuBox(
            expanded = topicExpanded,
            onExpandedChange = { topicExpanded = !topicExpanded },
        ) {
            OutlinedTextField(
                value = selectedTopic,
                onValueChange = {},
                readOnly = true,
                label = { Text("Topic") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = topicExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
            )
            ExposedDropdownMenu(
                expanded = topicExpanded,
                onDismissRequest = { topicExpanded = false },
            ) {
                topics.forEach { topic ->
                    DropdownMenuItem(
                        text = { Text(topic) },
                        onClick = {
                            selectedTopic = topic
                            topicExpanded = false
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = details,
            onValueChange = { details = it },
            label = { Text("Details") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { includeLogs = !includeLogs },
        ) {
            Checkbox(checked = includeLogs, onCheckedChange = { includeLogs = it })
            Spacer(Modifier.width(8.dp))
            Text("Include diagnostic logs")
        }

        Button(
            onClick = {
                val body = buildString {
                    append("Topic: $selectedTopic\n\n")
                    append(details)
                    if (includeLogs) append("\n\n[Logs attached]")
                }
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("support@taiga.io"))
                    putExtra(Intent.EXTRA_SUBJECT, "Taiga Android: $selectedTopic")
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                context.startActivity(Intent.createChooser(intent, "Send email"))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Send Support Email")
        }
    }
}
