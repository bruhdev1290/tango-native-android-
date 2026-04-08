package io.taiga.client.data.workspace

import io.taiga.client.data.models.IssueDetail
import io.taiga.client.data.models.IssueSummary
import io.taiga.client.data.models.ProjectSummary

interface TaigaWorkspaceRepository {
    suspend fun loadProjects(): List<ProjectSummary>
    suspend fun loadIssues(projectId: Long): List<IssueSummary>
    suspend fun loadIssue(issueId: Long): IssueDetail
}
