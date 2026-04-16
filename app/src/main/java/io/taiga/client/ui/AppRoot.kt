package io.taiga.client.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.taiga.client.data.auth.AuthState
import io.taiga.client.ui.navigation.Routes
import io.taiga.client.ui.screen.LockScreen
import io.taiga.client.ui.screen.LoginScreen
import io.taiga.client.ui.screen.ProjectBacklogScreen
import io.taiga.client.ui.screen.ProjectsShellScreen
import io.taiga.client.ui.screen.SettingsScreen
import io.taiga.client.ui.viewmodel.AppLockViewModel
import io.taiga.client.ui.viewmodel.AuthViewModel

@Composable
fun AppRoot(
    authViewModel: AuthViewModel = hiltViewModel(),
    lockViewModel: AppLockViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val formState by authViewModel.formState.collectAsStateWithLifecycle()
    val isLocked by lockViewModel.isLocked.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                lockViewModel.lockApp()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(authState) {
        val isAuthenticated = authState is AuthState.Authenticated
        val targetRoute = if (isAuthenticated) Routes.PROJECTS_SHELL else Routes.LOGIN

        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute != targetRoute &&
            currentRoute != Routes.SETTINGS &&
            currentRoute != Routes.PROJECT_BACKLOG
        ) {
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    val themeColor = MaterialTheme.colorScheme.primary.hashCode()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
        ) {
            composable(Routes.LOGIN) {
                key(themeColor) {
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
            }

            composable(Routes.PROJECTS_SHELL) {
                key(themeColor) {
                    ProjectsShellScreen(
                        onProjectClick = { projectId, projectName ->
                            navController.navigate(Routes.projectBacklog(projectId, projectName))
                        },
                        onOpenSettings = {
                            navController.navigate(Routes.SETTINGS)
                        },
                    )
                }
            }

            composable(
                route = Routes.PROJECT_BACKLOG,
                arguments = listOf(
                    navArgument(Routes.PROJECT_ID_ARG) { type = NavType.LongType },
                    navArgument(Routes.PROJECT_NAME_ARG) { type = NavType.StringType },
                ),
            ) {
                key(themeColor) {
                    ProjectBacklogScreen(
                        onBackClick = { navController.popBackStack() },
                    )
                }
            }

            composable(Routes.SETTINGS) {
                key(themeColor) {
                    SettingsScreen(
                        onDismiss = { navController.popBackStack() },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isLocked && authState is AuthState.Authenticated,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            LockScreen(
                isBiometricEnabled = lockViewModel.isBiometricEnabled(),
                onPinEntered = lockViewModel::unlockWithPin,
                onBiometricSuccess = lockViewModel::unlockWithBiometric,
            )
        }
    }
}
