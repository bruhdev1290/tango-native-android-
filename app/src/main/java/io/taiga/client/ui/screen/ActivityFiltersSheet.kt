package io.taiga.client.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.taiga.client.ui.viewmodel.ActivityFilters

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ActivityFiltersSheet(
    filters: ActivityFilters,
    onFiltersChange: (ActivityFilters) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Filters", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { onFiltersChange(ActivityFilters()) }) {
                    Text("Reset")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filters.showStories,
                    onClick = { onFiltersChange(filters.copy(showStories = !filters.showStories)) },
                    label = { Text("Stories") },
                )
                FilterChip(
                    selected = filters.showTasks,
                    onClick = { onFiltersChange(filters.copy(showTasks = !filters.showTasks)) },
                    label = { Text("Tasks") },
                )
                FilterChip(
                    selected = filters.showIssues,
                    onClick = { onFiltersChange(filters.copy(showIssues = !filters.showIssues)) },
                    label = { Text("Issues") },
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Scope", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filters.showAssigned,
                    onClick = { onFiltersChange(filters.copy(showAssigned = !filters.showAssigned)) },
                    label = { Text("Assigned to Me") },
                )
                FilterChip(
                    selected = filters.showInProjects,
                    onClick = { onFiltersChange(filters.copy(showInProjects = !filters.showInProjects)) },
                    label = { Text("In My Projects") },
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
