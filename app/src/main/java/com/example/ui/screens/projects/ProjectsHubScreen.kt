package com.example.ui.screens.projects

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.localization.LocalAppStrings
import com.example.core.util.Formatters
import com.example.data.local.entity.BtpConstants
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScheduleTaskEntity
import com.example.data.local.entity.SiteEntity
import com.example.ui.components.BtpBrandEmblem
import com.example.ui.components.BtpSegmentedTabs
import com.example.ui.components.BtpUnifiedModuleIcon
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ItemActionsRow
import com.example.ui.components.ProgressRing
import com.example.ui.components.SearchAndFilterRow
import com.example.ui.components.StatusChip
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusSuccess
import com.example.viewmodel.ProjectsUiState

@Composable
fun ProjectsHubScreen(
    uiState: ProjectsUiState,
    selectedSubTab: Int,
    onSubTabChange: (Int) -> Unit,
    currency: String,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onSaveProject: (ProjectEntity) -> Unit,
    onDeleteProject: (ProjectEntity) -> Unit,
    onSaveSite: (SiteEntity) -> Unit,
    onDeleteSite: (SiteEntity) -> Unit,
    onSaveTask: (ScheduleTaskEntity) -> Unit,
    onToggleTaskComplete: (ScheduleTaskEntity) -> Unit,
    onDeleteTask: (ScheduleTaskEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    var editingProject by remember { mutableStateOf<ProjectEntity?>(null) }
    var showProjectDialog by remember { mutableStateOf(false) }
    var deletingProject by remember { mutableStateOf<ProjectEntity?>(null) }

    var editingSite by remember { mutableStateOf<SiteEntity?>(null) }
    var showSiteDialog by remember { mutableStateOf(false) }
    var deletingSite by remember { mutableStateOf<SiteEntity?>(null) }

    var editingTask by remember { mutableStateOf<ScheduleTaskEntity?>(null) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var deletingTask by remember { mutableStateOf<ScheduleTaskEntity?>(null) }

    val projectNames = remember(uiState.projects) { uiState.projects.map { it.name } }
    val activeCount = when (selectedSubTab) {
        0 -> uiState.projects.size
        1 -> uiState.sites.size
        else -> uiState.tasks.size
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedSubTab) {
                        0 -> {
                            editingProject = null
                            showProjectDialog = true
                        }
                        1 -> {
                            editingSite = null
                            showSiteDialog = true
                        }
                        else -> {
                            editingTask = null
                            showTaskDialog = true
                        }
                    }
                },
                containerColor = AmberSecondary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("projects_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.addNew)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.projectsHubTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = AmberSecondary.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, AmberSecondary.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = "$activeCount",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = AmberSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BtpSegmentedTabs(
                tabs = listOf(strings.tabProjects, strings.tabSites, strings.tabSchedules),
                selectedIndex = selectedSubTab,
                onSelect = onSubTabChange
            )

            SearchAndFilterRow(
                query = uiState.searchQuery,
                onQueryChange = onSearchChange,
                placeholder = when (selectedSubTab) {
                    0 -> strings.searchProjectsHint
                    1 -> strings.searchSitesHint
                    else -> strings.searchTasksHint
                },
                filters = if (selectedSubTab == 0) listOf("الكل") + BtpConstants.projectStatuses else emptyList(),
                selectedFilter = uiState.selectedStatusFilter,
                onFilterSelect = onStatusFilterChange
            )

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val twoColumns = maxWidth >= 680.dp

                when (selectedSubTab) {
                    0 -> {
                        if (uiState.projects.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Outlined.Apartment,
                                title = strings.emptyProjectsTitle,
                                subtitle = strings.emptyProjectsSub,
                                actionLabel = strings.addProject,
                                onAction = {
                                    editingProject = null
                                    showProjectDialog = true
                                }
                            )
                        } else if (twoColumns) {
                            val rows = uiState.projects.chunked(2)
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(rows) { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { project ->
                                            ProjectItemCard(
                                                project = project,
                                                currency = currency,
                                                onEdit = {
                                                    editingProject = project
                                                    showProjectDialog = true
                                                },
                                                onDelete = { deletingProject = project },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.projects, key = { it.id }) { project ->
                                    ProjectItemCard(
                                        project = project,
                                        currency = currency,
                                        onEdit = {
                                            editingProject = project
                                            showProjectDialog = true
                                        },
                                        onDelete = { deletingProject = project }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        if (uiState.sites.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Outlined.Domain,
                                title = strings.emptySitesTitle,
                                subtitle = strings.emptySitesSub,
                                actionLabel = strings.addSite,
                                onAction = {
                                    editingSite = null
                                    showSiteDialog = true
                                }
                            )
                        } else if (twoColumns) {
                            val rows = uiState.sites.chunked(2)
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(rows) { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { site ->
                                            SiteItemCard(
                                                site = site,
                                                onEdit = {
                                                    editingSite = site
                                                    showSiteDialog = true
                                                },
                                                onDelete = { deletingSite = site },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.sites, key = { it.id }) { site ->
                                    SiteItemCard(
                                        site = site,
                                        onEdit = {
                                            editingSite = site
                                            showSiteDialog = true
                                        },
                                        onDelete = { deletingSite = site }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        if (uiState.tasks.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Outlined.CalendarMonth,
                                title = strings.emptyTasksTitle,
                                subtitle = strings.emptyTasksSub,
                                actionLabel = strings.addTask,
                                onAction = {
                                    editingTask = null
                                    showTaskDialog = true
                                }
                            )
                        } else if (twoColumns) {
                            val rows = uiState.tasks.chunked(2)
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(rows) { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { task ->
                                            ScheduleTaskCard(
                                                task = task,
                                                onToggleComplete = { onToggleTaskComplete(task) },
                                                onEdit = {
                                                    editingTask = task
                                                    showTaskDialog = true
                                                },
                                                onDelete = { deletingTask = task },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.tasks, key = { it.id }) { task ->
                                    ScheduleTaskCard(
                                        task = task,
                                        onToggleComplete = { onToggleTaskComplete(task) },
                                        onEdit = {
                                            editingTask = task
                                            showTaskDialog = true
                                        },
                                        onDelete = { deletingTask = task }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }

    if (showProjectDialog) {
        ProjectFormDialog(
            initial = editingProject,
            onSave = {
                onSaveProject(it)
                showProjectDialog = false
            },
            onDismiss = { showProjectDialog = false }
        )
    }

    if (showSiteDialog) {
        SiteFormDialog(
            initial = editingSite,
            projectNames = projectNames,
            onSave = {
                onSaveSite(it)
                showSiteDialog = false
            },
            onDismiss = { showSiteDialog = false }
        )
    }

    if (showTaskDialog) {
        ScheduleTaskFormDialog(
            initial = editingTask,
            projectNames = projectNames,
            onSave = {
                onSaveTask(it)
                showTaskDialog = false
            },
            onDismiss = { showTaskDialog = false }
        )
    }

    deletingProject?.let { p ->
        ConfirmDeleteDialog(
            title = strings.deleteProjectTitle,
            message = "${strings.confirmDeletePrefix} \"${p.name}\"؟",
            onConfirm = {
                onDeleteProject(p)
                deletingProject = null
            },
            onDismiss = { deletingProject = null }
        )
    }

    deletingSite?.let { s ->
        ConfirmDeleteDialog(
            title = strings.deleteSiteTitle,
            message = "${strings.confirmDeletePrefix} \"${s.name}\"؟",
            onConfirm = {
                onDeleteSite(s)
                deletingSite = null
            },
            onDismiss = { deletingSite = null }
        )
    }

    deletingTask?.let { t ->
        ConfirmDeleteDialog(
            title = strings.deleteTaskTitle,
            message = "${strings.confirmDeletePrefix} \"${t.title}\"؟",
            onConfirm = {
                onDeleteTask(t)
                deletingTask = null
            },
            onDismiss = { deletingTask = null }
        )
    }
}

@Composable
private fun ProjectItemCard(
    project: ProjectEntity,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    progressPercent = project.progressPercent,
                    color = if (project.progressPercent >= 75) StatusSuccess else AmberSecondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = project.code,
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        StatusChip(label = project.status)
                    }
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${project.clientName} • ${project.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (project.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = AmberSecondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${strings.budgetLabel} ${Formatters.formatMoney(project.budget, currency)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${strings.spentLabel} ${Formatters.formatMoney(project.spentAmount, currency)} • ${strings.deliveryLabel} ${project.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                ItemActionsRow(onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun SiteItemCard(
    site: SiteEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    BtpUnifiedModuleIcon(
                        icon = Icons.Outlined.Domain,
                        isAccentOrange = false,
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = site.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = site.projectName,
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberSecondary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip(label = site.safetyStatus)
                    StatusChip(label = site.status)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${strings.supervisorLabel} ${site.supervisorName}", style = MaterialTheme.typography.bodySmall)
                }
                Text("${strings.workersCountLabel} ${site.workerCount}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(15.dp), tint = AmberSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(site.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (site.dailyNote.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = site.dailyNote,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                ItemActionsRow(onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun ScheduleTaskCard(
    task: ScheduleTaskEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BtpUnifiedModuleIcon(
                icon = Icons.Outlined.EventNote,
                isAccentOrange = !task.isCompleted,
                size = 38.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusChip(label = task.priority)
                }
                Text(
                    text = "${task.projectName} • ${task.siteName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${strings.teamLabel} ${task.assignedTeam} • ${strings.dueDateLabel} ${task.dueDate}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AmberSecondary
                )
            }
            ItemActionsRow(onEdit = onEdit, onDelete = onDelete)
        }
    }
}
