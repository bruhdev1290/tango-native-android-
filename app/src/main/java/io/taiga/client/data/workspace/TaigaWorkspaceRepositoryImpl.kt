package io.taiga.client.data.workspace

import io.taiga.client.data.models.IssueDetail
import io.taiga.client.data.models.IssueSummary
import io.taiga.client.data.models.ProjectSummary
import io.taiga.client.data.models.SprintSummary
import io.taiga.client.data.models.TaskSummary
import io.taiga.client.data.models.UserMe
import io.taiga.client.data.models.UserStorySummary
import io.taiga.client.data.network.TaigaServiceFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaigaWorkspaceRepositoryImpl @Inject constructor(
    private val serviceFactory: TaigaServiceFactory,
) : TaigaWorkspaceRepository {
    override suspend fun loadProjects(member: Long?): List<ProjectSummary> =
        serviceFactory.createAuthenticatedApi().projects(slight = true, member = member)

    override suspend fun loadIssues(projectId: Long): List<IssueSummary> =
        serviceFactory.createAuthenticatedApi().issues(projectId = projectId)

    override suspend fun loadIssue(issueId: Long): IssueDetail =
        serviceFactory.createAuthenticatedApi().issue(issueId)

    override suspend fun getCurrentUser(): UserMe =
        serviceFactory.createAuthenticatedApi().me()

    override suspend fun getAssignedStories(userId: Long): List<UserStorySummary> =
        serviceFactory.createAuthenticatedApi().userStories(assignedTo = userId)

    override suspend fun getAssignedTasks(userId: Long): List<TaskSummary> =
        serviceFactory.createAuthenticatedApi().tasks(assignedTo = userId)

    override suspend fun getAssignedIssues(userId: Long): List<IssueSummary> =
        serviceFactory.createAuthenticatedApi().issues(assignedTo = userId)

    override suspend fun getProjectStories(projectId: Long): List<UserStorySummary> =
        serviceFactory.createAuthenticatedApi().userStories(projectId = projectId)

    override suspend fun getProjectTasks(projectId: Long): List<TaskSummary> =
        serviceFactory.createAuthenticatedApi().tasks(projectId = projectId)

    override suspend fun getSprints(projectId: Long): List<SprintSummary> =
        serviceFactory.createAuthenticatedApi().milestones(projectId)
}
