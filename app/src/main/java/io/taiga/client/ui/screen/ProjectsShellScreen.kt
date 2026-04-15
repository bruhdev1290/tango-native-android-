package io.taiga.client.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.taiga.client.ui.viewmodel.ProjectsShellViewModel
import io.taiga.client.ui.viewmodel.ShellTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsShellScreen(
    onProjectClick: (Long, String) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ProjectsShellViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pullRefreshState = rememberPullToRefreshState()

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refresh()
            pullRefreshState.endRefresh()
        }
    }

    if (state.showFiltersSheet) {
        ActivityFiltersSheet(
            filters = state.activityFilters,
            onFiltersChange = viewModel::setActivityFilters,
            onDismiss = viewModel::hideActivityFilters,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projects") },
                actions = {
                    if (state.selectedTab == ShellTab.HOME) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Search, contentDescription = "Search projects")
                        }
                    } else {
                        IconButton(onClick = viewModel::showActivityFilters) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter activity")
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                state.lastUpdated?.let { ts ->
                    val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
                    Text(
                        text = "Updated ${fmt.format(Date(ts))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp),
                    )
                }
                NavigationBar {
                    NavigationBarItem(
                        selected = state.selectedTab == ShellTab.HOME,
                        onClick = { viewModel.setTab(ShellTab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") },
                    )
                    NavigationBarItem(
                        selected = state.selectedTab == ShellTab.ACTIVITY,
                        onClick = { viewModel.setTab(ShellTab.ACTIVITY) },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                        label = { Text("Activity") },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(pullRefreshState.nestedScrollConnection),
        ) {
            if (state.loading && state.projects.isEmpty() && state.activityItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                AnimatedContent(
                    targetState = state.selectedTab,
                    modifier = Modifier.fillMaxSize(),
                    label = "tab_content",
                ) { tab ->
                    when (tab) {
                        ShellTab.HOME -> HomeTab(
                            username = state.currentUsername,
                            myWork = state.myWork,
                            projects = state.projects,
                            searchQuery = state.searchQuery,
                            onSearchQueryChange = viewModel::setSearchQuery,
                            onMyWorkClick = { projectId ->
                                val project = state.projects.find { it.id == projectId }
                                onProjectClick(projectId, project?.name ?: "")
                            },
                            onProjectClick = onProjectClick,
                        )
                        ShellTab.ACTIVITY -> ActivityTab(
                            activityItems = state.activityItems,
                            filters = state.activityFilters,
                            onItemClick = { projectId ->
                                val project = state.projects.find { it.id == projectId }
                                onProjectClick(projectId, project?.name ?: "")
                            },
                        )
                    }
                }
            }
            if (pullRefreshState.progress > 0f || pullRefreshState.isRefreshing) {
                PullToRefreshContainer(
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}
