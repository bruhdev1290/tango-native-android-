package io.taiga.client.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.taiga.client.data.auth.AuthState
import io.taiga.client.ui.navigation.Routes
import io.taiga.client.ui.screen.IssueDetailScreen
import io.taiga.client.ui.screen.IssueListScreen
import io.taiga.client.ui.screen.LoginScreen
import io.taiga.client.ui.screen.ProjectListScreen
import io.taiga.client.ui.viewmodel.AuthViewModel
import io.taiga.client.ui.viewmodel.IssueDetailViewModel
import io.taiga.client.ui.viewmodel.IssueListViewModel
import io.taiga.client.ui.viewmodel.ProjectListViewModel

@Composable
fun AppRoot(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val formState by authViewModel.formState.collectAsStateWithLifecycle()

    // Navigate based on authentication state
    LaunchedEffect(authState) {
        val isAuthenticated = authState is AuthState.Authenticated
        val targetRoute = if (isAuthenticated) Routes.PROJECTS else Routes.LOGIN

        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute != targetRoute) {
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authState = authState,
                formState = formState,
                onBaseUrlChanged = authViewModel::onBaseUrlChanged,
                onUsernameChanged = authViewModel::onUsernameChanged,
                onPasswordChanged = authViewModel::onPasswordChanged,
                onGitHubCodeChanged = authViewModel::onGitHubCodeChanged,
                onNormalLoginClick = authViewModel::login,
                onGitHubLoginClick = authViewModel::loginWithGitHub,
                onClearError = authViewModel::clearError,
            )
        }

        composable(Routes.PROJECTS) {
            val projectListViewModel: ProjectListViewModel = hiltViewModel()
            val projectState by projectListViewModel.uiState.collectAsStateWithLifecycle()

            ProjectListScreen(
                state = projectState,
                onProjectClick = { project ->
                    navController.navigate(Routes.issues(project.id, project.name))
                },
                onRetryClick = projectListViewModel::loadProjects,
                onLogoutClick = authViewModel::logout,
                onSearchQueryChanged = projectListViewModel::onSearchQueryChanged,
                onVisibilityFilterChanged = projectListViewModel::onVisibilityFilterChanged,
            )
        }

        composable(
            route = Routes.ISSUES,
            arguments = listOf(
                navArgument(Routes.PROJECT_ID_ARG) { type = NavType.LongType },
                navArgument(Routes.PROJECT_NAME_ARG) { type = NavType.StringType },
            ),
        ) {
            val issueListViewModel: IssueListViewModel = hiltViewModel()
            val issueState by issueListViewModel.uiState.collectAsStateWithLifecycle()

            IssueListScreen(
                state = issueState,
                onBackClick = { navController.popBackStack() },
                onRetryClick = issueListViewModel::loadIssues,
                onLogoutClick = authViewModel::logout,
                onIssueClick = { issue -> navController.navigate(Routes.issueDetail(issue.id)) },
            )
        }

        composable(
            route = Routes.ISSUE_DETAIL,
            arguments = listOf(
                navArgument(Routes.ISSUE_ID_ARG) { type = NavType.LongType },
            ),
        ) {
            val issueDetailViewModel: IssueDetailViewModel = hiltViewModel()
            val issueDetailState by issueDetailViewModel.uiState.collectAsStateWithLifecycle()

            IssueDetailScreen(
                state = issueDetailState,
                onBackClick = { navController.popBackStack() },
                onRetryClick = issueDetailViewModel::loadIssue,
                onLogoutClick = authViewModel::logout,
            )
        }
    }
}
