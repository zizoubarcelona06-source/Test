package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScheduleTaskEntity
import com.example.data.repository.BtpRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class DashboardAlertType {
    LOW_STOCK,
    OVERDUE_INVOICE,
    MAINTENANCE_EQUIPMENT,
    SAFETY_ALERT
}

data class DashboardAlert(
    val type: DashboardAlertType,
    val subject: String,
    val detailPrimary: String,
    val detailSecondary: String = "",
    val isUrgent: Boolean
)

data class DashboardUiState(
    val activeProjectsCount: Int = 0,
    val totalProjectsCount: Int = 0,
    val activeSitesCount: Int = 0,
    val presentEmployeesCount: Int = 0,
    val totalEmployeesCount: Int = 0,
    val activeEquipmentCount: Int = 0,
    val maintenanceEquipmentCount: Int = 0,
    val lowStockCount: Int = 0,
    val totalCollectedRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val pendingReceivables: Double = 0.0,
    val overdueInvoicesCount: Int = 0,
    val recentProjects: List<ProjectEntity> = emptyList(),
    val upcomingTasks: List<ScheduleTaskEntity> = emptyList(),
    val alerts: List<DashboardAlert> = emptyList()
) {
    val netBalance: Double
        get() = totalCollectedRevenue - totalExpenses
}

class DashboardViewModel(repository: BtpRepository) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.projects,
        repository.sites,
        repository.employees,
        repository.equipment,
        repository.materials,
        repository.expenses,
        repository.invoices,
        repository.scheduleTasks
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val projects = values[0] as List<com.example.data.local.entity.ProjectEntity>
        @Suppress("UNCHECKED_CAST")
        val sites = values[1] as List<com.example.data.local.entity.SiteEntity>
        @Suppress("UNCHECKED_CAST")
        val employees = values[2] as List<com.example.data.local.entity.EmployeeEntity>
        @Suppress("UNCHECKED_CAST")
        val equipment = values[3] as List<com.example.data.local.entity.EquipmentEntity>
        @Suppress("UNCHECKED_CAST")
        val materials = values[4] as List<com.example.data.local.entity.MaterialEntity>
        @Suppress("UNCHECKED_CAST")
        val expenses = values[5] as List<com.example.data.local.entity.ExpenseEntity>
        @Suppress("UNCHECKED_CAST")
        val invoices = values[6] as List<com.example.data.local.entity.InvoiceEntity>
        @Suppress("UNCHECKED_CAST")
        val tasks = values[7] as List<com.example.data.local.entity.ScheduleTaskEntity>

        val lowStockItems = materials.filter { it.isLowStock }
        val maintenanceEq = equipment.filter { it.status == "صيانة" }
        val overdueInvoices = invoices.filter { it.status == "متأخرة" }
        val alertSites = sites.filter { it.safetyStatus != "آمن" }

        val alerts = buildList {
            lowStockItems.forEach {
                add(
                    DashboardAlert(
                        type = DashboardAlertType.LOW_STOCK,
                        subject = it.name,
                        detailPrimary = "${it.quantityInStock} ${it.unit}",
                        detailSecondary = "${it.minThreshold}",
                        isUrgent = true
                    )
                )
            }
            overdueInvoices.forEach {
                add(
                    DashboardAlert(
                        type = DashboardAlertType.OVERDUE_INVOICE,
                        subject = it.invoiceNumber,
                        detailPrimary = it.partyName,
                        detailSecondary = it.dueDate,
                        isUrgent = true
                    )
                )
            }
            maintenanceEq.forEach {
                add(
                    DashboardAlert(
                        type = DashboardAlertType.MAINTENANCE_EQUIPMENT,
                        subject = it.name,
                        detailPrimary = it.assignedSiteName,
                        detailSecondary = it.nextMaintenanceDate,
                        isUrgent = false
                    )
                )
            }
            alertSites.forEach {
                add(
                    DashboardAlert(
                        type = DashboardAlertType.SAFETY_ALERT,
                        subject = it.name,
                        detailPrimary = it.dailyNote,
                        isUrgent = it.safetyStatus == "طارئ"
                    )
                )
            }
        }

        val clientInvoices = invoices.filter { it.type == "فاتورة عميل" }
        val collected = clientInvoices.sumOf { it.paidAmount }
        val pendingRecv = clientInvoices.sumOf { it.remainingAmount }
        val totalExp = expenses.sumOf { it.amount }

        DashboardUiState(
            activeProjectsCount = projects.count { it.status == "قيد التنفيذ" },
            totalProjectsCount = projects.size,
            activeSitesCount = sites.count { it.status == "نشط" },
            presentEmployeesCount = employees.count { it.attendanceStatus == "حاضر" },
            totalEmployeesCount = employees.size,
            activeEquipmentCount = equipment.count { it.status == "في الخدمة" },
            maintenanceEquipmentCount = maintenanceEq.size,
            lowStockCount = lowStockItems.size,
            totalCollectedRevenue = collected,
            totalExpenses = totalExp,
            pendingReceivables = pendingRecv,
            overdueInvoicesCount = overdueInvoices.size,
            recentProjects = projects.take(4),
            upcomingTasks = tasks.filter { !it.isCompleted }.take(4),
            alerts = alerts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
