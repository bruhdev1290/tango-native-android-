package io.taiga.client.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.taiga.client.ui.viewmodel.IssueDetailUiState

@Composable
fun IssueDetailScreen(
    state: IssueDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Issue detail", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text("Full issue metadata from Taiga.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onBackClick) { Text("Back") }
                Button(onClick = onLogoutClick) { Text("Log out") }
            }
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

        val issue = state.issue
        if (issue == null) {
            Text("Issue not found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("#${issue.ref} ${issue.subject}", style = MaterialTheme.typography.titleLarge)
                    issue.project?.let {
                        Text(it.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    issue.status?.name?.let { Text("Status: $it") }
                    issue.type?.name?.let { Text("Type: $it") }
                    issue.priority?.name?.let { Text("Priority: $it") }
                    issue.severity?.name?.let { Text("Severity: $it") }
                    issue.owner?.full_name_display?.let { Text("Owner: $it") }
                    issue.assigned_to?.full_name_display?.let { Text("Assigned to: $it") }
                    issue.due_date?.let { Text("Due date: $it") }
                    issue.created_date?.let { Text("Created: $it") }
                    issue.modified_date?.let { Text("Modified: $it") }
                    issue.finished_date?.let { Text("Finished: $it") }
                    issue.is_blocked?.let { Text("Blocked: $it") }
                    issue.blocked_note?.takeIf { it.isNotBlank() }?.let { note ->
                        Divider()
                        Text(note)
                    }
                    issue.description?.takeIf { it.isNotBlank() }?.let { description ->
                        Divider()
                        Text(description)
                    }
                    issue.description_html?.takeIf { it.isNotBlank() }?.let {
                        Divider()
                        Text("HTML description is available in Taiga.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
