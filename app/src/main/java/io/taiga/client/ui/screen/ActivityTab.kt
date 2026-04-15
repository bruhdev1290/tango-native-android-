package io.taiga.client.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.taiga.client.data.models.ActivityItem
import io.taiga.client.data.models.ActivityType
import io.taiga.client.data.models.ItemKind
import io.taiga.client.data.models.ItemScope
import io.taiga.client.ui.viewmodel.ActivityFilters

@Composable
fun ActivityTab(
    activityItems: List<ActivityItem>,
    filters: ActivityFilters,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filtered = activityItems.filter { item ->
        val kindMatch = when (item.kind) {
            ItemKind.STORY -> filters.showStories
            ItemKind.TASK -> filters.showTasks
            ItemKind.ISSUE -> filters.showIssues
        }
        val scopeMatch = when (item.scope) {
            ItemScope.ASSIGNED -> filters.showAssigned
            ItemScope.PROJECT -> filters.showInProjects
        }
        kindMatch && scopeMatch
    }

    if (filtered.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "No recent activity",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(filtered) { item ->
                ActivityRow(
                    item = item,
                    onClick = { onItemClick(item.projectId) },
                )
            }
        }
    }
}

@Composable
private fun ActivityRow(
    item: ActivityItem,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(item.subject, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Column {
                val description = item.description
                if (!description.isNullOrBlank()) {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (item.projectName.isNotBlank()) {
                    Text(
                        item.projectName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        leadingContent = {
            ProjectInitialsAvatar(name = item.projectName)
        },
        trailingContent = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ActivityTypeBadge(type = item.activityType)
                KindBadge(kind = item.kind)
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
fun ActivityTypeBadge(type: ActivityType, modifier: Modifier = Modifier) {
    val label = when (type) {
        ActivityType.ASSIGNED -> "Assigned"
        ActivityType.NEW_ITEM -> "New"
        ActivityType.CHANGED -> "Changed"
        ActivityType.PROJECT -> "Project"
        ActivityType.NOTIFICATION -> "Alert"
        ActivityType.USER_ADDED -> "Member"
        ActivityType.PING -> "Ping"
    }
    val bgColor = when (type) {
        ActivityType.ASSIGNED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ActivityType.NEW_ITEM -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
        ActivityType.CHANGED -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        ActivityType.PROJECT -> MaterialTheme.colorScheme.surfaceVariant
        ActivityType.NOTIFICATION -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
        ActivityType.USER_ADDED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ActivityType.PING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    }
    val textColor = when (type) {
        ActivityType.ASSIGNED -> MaterialTheme.colorScheme.primary
        ActivityType.NEW_ITEM -> MaterialTheme.colorScheme.tertiary
        ActivityType.CHANGED -> MaterialTheme.colorScheme.secondary
        ActivityType.PROJECT -> MaterialTheme.colorScheme.onSurfaceVariant
        ActivityType.NOTIFICATION -> MaterialTheme.colorScheme.error
        ActivityType.USER_ADDED -> MaterialTheme.colorScheme.primary
        ActivityType.PING -> MaterialTheme.colorScheme.tertiary
    }
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}
