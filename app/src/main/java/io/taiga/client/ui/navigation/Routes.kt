package io.taiga.client.ui.navigation

import android.net.Uri

object Routes {
    const val LOGIN = "login"
    const val PROJECTS_SHELL = "projects_shell"
    const val PROJECT_BACKLOG = "project_backlog/{projectId}/{projectName}"
    const val SETTINGS = "settings"

    // Legacy routes (kept for reference, not used in new nav graph)
    const val PROJECTS = "projects"
    const val ISSUES = "issues/{projectId}/{projectName}"
    const val ISSUE_DETAIL = "issue/{issueId}"

    const val PROJECT_ID_ARG = "projectId"
    const val PROJECT_NAME_ARG = "projectName"
    const val ISSUE_ID_ARG = "issueId"

    fun projectBacklog(projectId: Long, projectName: String): String =
        "project_backlog/$projectId/${Uri.encode(projectName)}"

    // Legacy helpers
    fun issues(projectId: Long, projectName: String): String =
        "issues/$projectId/${Uri.encode(projectName)}"

    fun issueDetail(issueId: Long): String = "issue/$issueId"
}
