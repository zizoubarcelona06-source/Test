package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScheduleTaskEntity
import com.example.data.local.entity.SiteEntity
import com.example.data.repository.BtpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProjectsUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val sites: List<SiteEntity> = emptyList(),
    val tasks: List<ScheduleTaskEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: String = "الكل"
)

class ProjectsViewModel(private val repository: BtpRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val statusFilter = MutableStateFlow("الكل")

    val uiState: StateFlow<ProjectsUiState> = combine(
        repository.projects,
        repository.sites,
        repository.scheduleTasks,
        searchQuery,
        statusFilter
    ) { projects, sites, tasks, query, filter ->
        val q = query.trim().lowercase()
        val filteredProjects = projects.filter { p ->
            (filter == "الكل" || p.status == filter) &&
                (q.isEmpty() || p.name.lowercase().contains(q) || p.clientName.lowercase().contains(q) || p.code.lowercase().contains(q))
        }
        val filteredSites = sites.filter { s ->
            q.isEmpty() || s.name.lowercase().contains(q) || s.projectName.lowercase().contains(q) || s.supervisorName.lowercase().contains(q)
        }
        val filteredTasks = tasks.filter { t ->
            q.isEmpty() || t.title.lowercase().contains(q) || t.projectName.lowercase().contains(q) || t.assignedTeam.lowercase().contains(q)
        }
        ProjectsUiState(
            projects = filteredProjects,
            sites = filteredSites,
            tasks = filteredTasks,
            searchQuery = query,
            selectedStatusFilter = filter
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProjectsUiState())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setStatusFilter(status: String) {
        statusFilter.value = status
    }

    fun saveProject(project: ProjectEntity) {
        viewModelScope.launch { repository.saveProject(project) }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch { repository.deleteProject(project) }
    }

    fun saveSite(site: SiteEntity) {
        viewModelScope.launch { repository.saveSite(site) }
    }

    fun deleteSite(site: SiteEntity) {
        viewModelScope.launch { repository.deleteSite(site) }
    }

    fun saveTask(task: ScheduleTaskEntity) {
        viewModelScope.launch { repository.saveScheduleTask(task) }
    }

    fun toggleTaskComplete(task: ScheduleTaskEntity) {
        viewModelScope.launch {
            val completed = !task.isCompleted
            repository.saveScheduleTask(
                task.copy(
                    isCompleted = completed,
                    progress = if (completed) 100 else task.progress.coerceAtMost(90)
                )
            )
        }
    }

    fun deleteTask(task: ScheduleTaskEntity) {
        viewModelScope.launch { repository.deleteScheduleTask(task) }
    }
}
