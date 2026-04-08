package io.taiga.client.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.taiga.client.data.models.ProjectSummary
import io.taiga.client.ui.viewmodel.ProjectVisibilityFilter
import io.taiga.client.ui.viewmodel.ProjectListUiState

@Composable
fun ProjectListScreen(
    state: ProjectListUiState,
    onProjectClick: (ProjectSummary) -> Unit,
    onRetryClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onVisibilityFilterChanged: (ProjectVisibilityFilter) -> Unit,
) {
    val filteredProjects = state.projects.filter { project ->
        val query = state.searchQuery.trim().lowercase()
        val matchesQuery = query.isEmpty() || listOfNotNull(
            project.name,
            project.slug,
            project.description,
        ).any { it.lowercase().contains(query) }

        val matchesVisibility = when (state.visibilityFilter) {
            ProjectVisibilityFilter.ALL -> true
            ProjectVisibilityFilter.PUBLIC -> project.is_private != true
            ProjectVisibilityFilter.PRIVATE -> project.is_private == true
        }

        matchesQuery && matchesVisibility
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Taiga Projects", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text("Search and filter the projects you can access.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onLogoutClick) {
                Text("Log out")
            }
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.searchQuery,
            onValueChange = onSearchQueryChanged,
            label = { Text("Search projects") },
            singleLine = true,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { onVisibilityFilterChanged(ProjectVisibilityFilter.ALL) }, label = { Text("All") })
            AssistChip(onClick = { onVisibilityFilterChanged(ProjectVisibilityFilter.PUBLIC) }, label = { Text("Public") })
            AssistChip(onClick = { onVisibilityFilterChanged(ProjectVisibilityFilter.PRIVATE) }, label = { Text("Private") })
        }

        if (state.isLoading) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
            }
            return
        }

        state.errorMessage?.let { errorMessage ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
                Button(onClick = onRetryClick) {
                    Text("Retry")
                }
            }
        }

        if (filteredProjects.isEmpty()) {
            Text("No projects found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filteredProjects, key = { it.id }) { project ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProjectClick(project) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(project.name, style = MaterialTheme.typography.titleLarge)
                            Text(project.slug, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            project.description?.takeIf { it.isNotBlank() }?.let {
                                Divider()
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
