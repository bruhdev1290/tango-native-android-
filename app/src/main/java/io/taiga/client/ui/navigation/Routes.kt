package io.taiga.client.ui.navigation

import android.net.Uri

object Routes {
    const val LOGIN = "login"
    const val PROJECTS = "projects"
    const val ISSUES = "issues/{projectId}/{projectName}"
    const val ISSUE_DETAIL = "issue/{issueId}"

    const val PROJECT_ID_ARG = "projectId"
    const val PROJECT_NAME_ARG = "projectName"
    const val ISSUE_ID_ARG = "issueId"

    fun issues(projectId: Long, projectName: String): String {
        return "issues/$projectId/${Uri.encode(projectName)}"
    }

    fun issueDetail(issueId: Long): String {
        return "issue/$issueId"
    }
}
