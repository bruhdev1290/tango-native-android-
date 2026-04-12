package io.taiga.client.data.workspace

import io.taiga.client.data.models.IssueDetail
import io.taiga.client.data.models.IssueSummary
import io.taiga.client.data.models.ProjectSummary
import io.taiga.client.data.network.TaigaServiceFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaigaWorkspaceRepositoryImpl @Inject constructor(
    private val serviceFactory: TaigaServiceFactory,
) : TaigaWorkspaceRepository {
    override suspend fun loadProjects(): List<ProjectSummary> {
        return serviceFactory.createAuthenticatedApi().projects(slight = true)
    }

    override suspend fun loadIssues(projectId: Long): List<IssueSummary> {
        return serviceFactory.createAuthenticatedApi().issues(projectId)
    }

    override suspend fun loadIssue(issueId: Long): IssueDetail {
        return serviceFactory.createAuthenticatedApi().issue(issueId)
    }
}
