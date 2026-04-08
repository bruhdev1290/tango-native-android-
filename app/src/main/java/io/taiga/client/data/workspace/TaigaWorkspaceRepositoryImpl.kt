package io.taiga.client.data.workspace

import io.taiga.client.data.models.IssueDetail
import io.taiga.client.data.models.IssueSummary
import io.taiga.client.data.models.ProjectSummary
import io.taiga.client.data.network.TaigaTokenAuthenticator
import io.taiga.client.data.network.TaigaServiceFactory
import io.taiga.client.data.session.SecureSessionStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaigaWorkspaceRepositoryImpl @Inject constructor(
    private val serviceFactory: TaigaServiceFactory,
    private val authenticator: TaigaTokenAuthenticator,
    private val sessionStore: SecureSessionStore,
) : TaigaWorkspaceRepository {
    override suspend fun loadProjects(): List<ProjectSummary> {
        val session = requireSession()
        return serviceFactory.createTaigaApi(session.baseUrl, session.authToken, authenticator)
            .projects(slight = true)
    }

    override suspend fun loadIssues(projectId: Long): List<IssueSummary> {
        val session = requireSession()
        return serviceFactory.createTaigaApi(session.baseUrl, session.authToken, authenticator)
            .issues(projectId)
    }

    override suspend fun loadIssue(issueId: Long): IssueDetail {
        val session = requireSession()
        return serviceFactory.createTaigaApi(session.baseUrl, session.authToken, authenticator)
            .issue(issueId)
    }

    private fun requireSession() = sessionStore.readSession()
        ?: error("No active Taiga session")
}
