package io.taiga.client.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.taiga.client.data.models.ActivityItem
import io.taiga.client.data.models.ItemKind
import io.taiga.client.data.models.ProjectSummary

@Composable
fun HomeTab(
    username: String,
    myWork: List<ActivityItem>,
    projects: List<ProjectSummary>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMyWorkClick: (Long) -> Unit,
    onProjectClick: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val query = searchQuery.trim().lowercase()
    val filteredProjects = projects.filter { project ->
        query.isEmpty() || listOfNotNull(
            project.name,
            project.slug,
            project.description,
        ).any { it.lowercase().contains(query) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            Text(
                text = "Hello, $username",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search projects...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        if (myWork.isNotEmpty()) {
            item {
                Text(
                    text = "My Work",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(myWork) { item ->
                        MyWorkCard(
                            item = item,
                            onClick = { onMyWorkClick(item.projectId) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        item {
            Text(
                text = "Projects",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        if (filteredProjects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (projects.isEmpty()) "No projects yet" else "No projects found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(filteredProjects) { project ->
                ProjectRow(
                    project = project,
                    onClick = { onProjectClick(project.id, project.name) },
                )
            }
        }
    }
}

@Composable
private fun MyWorkCard(
    item: ActivityItem,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .height(120.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = item.subject,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                KindBadge(kind = item.kind)
                if (item.projectName.isNotBlank()) {
                    Text(
                        text = item.projectName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectRow(
    project: ProjectSummary,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(project.name) },
        supportingContent = project.description?.takeIf { it.isNotBlank() }?.let {
            { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        },
        leadingContent = {
            ProjectInitialsAvatar(name = project.name)
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
fun ProjectInitialsAvatar(name: String, modifier: Modifier = Modifier) {
    val initials = name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        Text(
            text = initials.ifBlank { "?" },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun KindBadge(kind: ItemKind, modifier: Modifier = Modifier) {
    val label = when (kind) {
        ItemKind.STORY -> "Story"
        ItemKind.TASK -> "Task"
        ItemKind.ISSUE -> "Issue"
    }
    val color = when (kind) {
        ItemKind.STORY -> MaterialTheme.colorScheme.primaryContainer
        ItemKind.TASK -> MaterialTheme.colorScheme.secondaryContainer
        ItemKind.ISSUE -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val onColor = when (kind) {
        ItemKind.STORY -> MaterialTheme.colorScheme.onPrimaryContainer
        ItemKind.TASK -> MaterialTheme.colorScheme.onSecondaryContainer
        ItemKind.ISSUE -> MaterialTheme.colorScheme.onTertiaryContainer
    }
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(color)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = onColor,
        )
    }
}
