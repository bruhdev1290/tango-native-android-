package io.taiga.client.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.taiga.client.data.models.ItemKind
import io.taiga.client.data.models.SprintSummary
import io.taiga.client.ui.viewmodel.DisplayFilter
import io.taiga.client.ui.viewmodel.ProjectBacklogViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectBacklogScreen(
    onBackClick: () -> Unit,
    viewModel: ProjectBacklogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showFabMenu by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refresh()
            pullRefreshState.endRefresh()
        }
    }

    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (state.showAddSheet) {
        AddItemSheet(
            title = "New ${state.addSheetTarget?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Item"}",
            onSave = { subject ->
                when (state.addSheetTarget) {
                    ItemKind.STORY -> viewModel.addStory(subject)
                    ItemKind.TASK -> viewModel.addTask(subject)
                    ItemKind.ISSUE -> viewModel.addIssue(subject)
                    null -> viewModel.hideAddSheet()
                }
            },
            onDismiss = viewModel::hideAddSheet,
            sheetState = addSheetState,
        )
    }
    state.editItem?.let { editItem ->
        AddItemSheet(
            title = "Edit ${editItem.kind.name.lowercase().replaceFirstChar { it.uppercase() }}",
            initialValue = editItem.currentSubject,
            onSave = { subject -> viewModel.editItem(editItem.kind, editItem.id, subject) },
            onDismiss = viewModel::hideEditSheet,
            sheetState = editSheetState,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.projectName.ifBlank { "Backlog" }) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showFabMenu = true }) {
                    Icon(Icons.Default.Add, "Add item")
                }
                if (showFabMenu) {
                    DropdownMenu(expanded = true, onDismissRequest = { showFabMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("New Story") },
                            onClick = { showFabMenu = false; viewModel.showAddSheet(ItemKind.STORY) },
                        )
                        DropdownMenuItem(
                            text = { Text("New Task") },
                            onClick = { showFabMenu = false; viewModel.showAddSheet(ItemKind.TASK) },
                        )
                        DropdownMenuItem(
                            text = { Text("New Issue") },
                            onClick = { showFabMenu = false; viewModel.showAddSheet(ItemKind.ISSUE) },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Search...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DisplayFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.displayFilter == filter,
                        onClick = { viewModel.setDisplayFilter(filter) },
                        label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(pullRefreshState.nestedScrollConnection),
            ) {
                if (state.loading && state.stories.isEmpty() && state.tasks.isEmpty() && state.issues.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val query = state.searchQuery.lowercase()
                    val filteredStories = state.stories.filter { query.isBlank() || it.subject.lowercase().contains(query) }
                    val filteredTasks = state.tasks.filter { query.isBlank() || it.subject.lowercase().contains(query) }
                    val filteredIssues = state.issues.filter { query.isBlank() || it.subject.lowercase().contains(query) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp),
                    ) {
                        if (state.displayFilter == DisplayFilter.ALL || state.displayFilter == DisplayFilter.SPRINTS) {
                            state.sprints.forEach { sprint ->
                                item(key = "sprint_header_${sprint.id}") {
                                    SprintHeader(sprint)
                                }
                                val sprintStories = filteredStories.filter { it.milestone == sprint.id }
                                items(sprintStories, key = { "sprint_story_${it.id}" }) { story ->
                                    SwipeableBacklogItem(
                                        subject = story.subject,
                                        kind = ItemKind.STORY,
                                        isCompleted = "STORY:${story.id}" in state.completedItemIds,
                                        onEdit = { viewModel.showEditSheet(ItemKind.STORY, story.id, story.subject) },
                                        onToggleComplete = { viewModel.toggleComplete(ItemKind.STORY, story.id) },
                                        onDelete = { viewModel.deleteItem(ItemKind.STORY, story.id) },
                                    )
                                }
                            }
                        }

                        if (state.displayFilter == DisplayFilter.ALL || state.displayFilter == DisplayFilter.STORIES) {
                            val backlogStories = if (state.displayFilter == DisplayFilter.ALL)
                                filteredStories.filter { it.milestone == null }
                            else filteredStories
                            if (backlogStories.isNotEmpty()) {
                                item(key = "stories_header") { SectionHeader("User Stories") }
                                items(backlogStories, key = { "story_${it.id}" }) { story ->
                                    SwipeableBacklogItem(
                                        subject = story.subject,
                                        kind = ItemKind.STORY,
                                        isCompleted = "STORY:${story.id}" in state.completedItemIds,
                                        onEdit = { viewModel.showEditSheet(ItemKind.STORY, story.id, story.subject) },
                                        onToggleComplete = { viewModel.toggleComplete(ItemKind.STORY, story.id) },
                                        onDelete = { viewModel.deleteItem(ItemKind.STORY, story.id) },
                                    )
                                }
                            }
                        }

                        if (state.displayFilter == DisplayFilter.ALL || state.displayFilter == DisplayFilter.TASKS) {
                            if (filteredTasks.isNotEmpty()) {
                                item(key = "tasks_header") { SectionHeader("Tasks") }
                                items(filteredTasks, key = { "task_${it.id}" }) { task ->
                                    SwipeableBacklogItem(
                                        subject = task.subject,
                                        kind = ItemKind.TASK,
                                        isCompleted = "TASK:${task.id}" in state.completedItemIds,
                                        onEdit = { viewModel.showEditSheet(ItemKind.TASK, task.id, task.subject) },
                                        onToggleComplete = { viewModel.toggleComplete(ItemKind.TASK, task.id) },
                                        onDelete = { viewModel.deleteItem(ItemKind.TASK, task.id) },
                                    )
                                }
                            }
                        }

                        if (state.displayFilter == DisplayFilter.ALL || state.displayFilter == DisplayFilter.ISSUES) {
                            if (filteredIssues.isNotEmpty()) {
                                item(key = "issues_header") { SectionHeader("Issues") }
                                items(filteredIssues, key = { "issue_${it.id}" }) { issue ->
                                    SwipeableBacklogItem(
                                        subject = issue.subject,
                                        kind = ItemKind.ISSUE,
                                        isCompleted = "ISSUE:${issue.id}" in state.completedItemIds,
                                        onEdit = { viewModel.showEditSheet(ItemKind.ISSUE, issue.id, issue.subject) },
                                        onToggleComplete = { viewModel.toggleComplete(ItemKind.ISSUE, issue.id) },
                                        onDelete = { viewModel.deleteItem(ItemKind.ISSUE, issue.id) },
                                    )
                                }
                            }
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
}

@Composable
private fun SprintHeader(sprint: SprintSummary) {
    Column {
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = sprint.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (sprint.closed) {
                Text(
                    "Closed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        HorizontalDivider()
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableBacklogItem(
    subject: String,
    kind: ItemKind,
    isCompleted: Boolean,
    onEdit: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                label = "swipe_bg"
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 16.dp),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                    Alignment.CenterStart else Alignment.CenterEnd,
            ) {
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd ->
                        Icon(Icons.Default.Edit, "Edit", tint = Color.White)
                    SwipeToDismissBoxValue.EndToStart ->
                        Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                    else -> {}
                }
            }
        },
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = subject,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
            },
            leadingContent = { KindBadge(kind = kind) },
            trailingContent = {
                IconButton(onClick = onToggleComplete) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = if (isCompleted) "Undo" else "Complete",
                        tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        )
    }
    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemSheet(
    title: String,
    initialValue: String = "",
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
) {
    var subject by remember(initialValue) { mutableStateOf(initialValue) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSave(subject) }),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onSave(subject) }) { Text("Save") }
            }
        }
    }
}
