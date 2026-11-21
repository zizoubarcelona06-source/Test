package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.EquipmentEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.MaterialEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScheduleTaskEntity
import com.example.data.repository.BtpRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CategoryAmount(
    val label: String,
    val amount: Double
)

data class ProjectBudgetStat(
    val name: String,
    val budget: Double,
    val spent: Double,
    val progressPercent: Int
)

data class ReportsUiState(
    val totalProjects: Int = 0,
    val averageProjectProgress: Int = 0,
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalClientInvoices: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val expensesByCategory: List<CategoryAmount> = emptyList(),
    val projectBudgetStats: List<ProjectBudgetStat> = emptyList(),
    val presentWorkers: Int = 0,
    val totalWorkers: Int = 0,
    val dailyPayrollCost: Double = 0.0,
    val activeEquipment: Int = 0,
    val totalEquipment: Int = 0,
    val inventoryValue: Double = 0.0,
    val lowStockCount: Int = 0,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0
)

class ReportsViewModel(repository: BtpRepository) : ViewModel() {

    val uiState: StateFlow<ReportsUiState> = combine(
        repository.projects,
        repository.expenses,
        repository.invoices,
        repository.employees,
        repository.equipment,
        repository.materials,
        repository.scheduleTasks
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val projects = values[0] as List<ProjectEntity>
        @Suppress("UNCHECKED_CAST")
        val expenses = values[1] as List<ExpenseEntity>
        @Suppress("UNCHECKED_CAST")
        val invoices = values[2] as List<InvoiceEntity>
        @Suppress("UNCHECKED_CAST")
        val employees = values[3] as List<EmployeeEntity>
        @Suppress("UNCHECKED_CAST")
        val equipment = values[4] as List<EquipmentEntity>
        @Suppress("UNCHECKED_CAST")
        val materials = values[5] as List<MaterialEntity>
        @Suppress("UNCHECKED_CAST")
        val tasks = values[6] as List<ScheduleTaskEntity>

        val groupedExpenses = expenses
            .groupBy { it.category }
            .map { (cat, list) -> CategoryAmount(cat, list.sumOf { it.amount }) }
            .sortedByDescending { it.amount }

        val clientInvoices = invoices.filter { it.type == "فاتورة عميل" }
        val presentEmployees = employees.filter { it.attendanceStatus == "حاضر" }

        ReportsUiState(
            totalProjects = projects.size,
            averageProjectProgress = if (projects.isEmpty()) 0 else projects.sumOf { it.progressPercent } / projects.size,
            totalBudget = projects.sumOf { it.budget },
            totalSpent = projects.sumOf { it.spentAmount },
            totalClientInvoices = clientInvoices.sumOf { it.totalAmount },
            totalCollected = clientInvoices.sumOf { it.paidAmount },
            totalExpenses = expenses.sumOf { it.amount },
            expensesByCategory = groupedExpenses,
            projectBudgetStats = projects.take(5).map {
                ProjectBudgetStat(
                    name = it.name,
                    budget = it.budget,
                    spent = it.spentAmount,
                    progressPercent = it.progressPercent
                )
            },
            presentWorkers = presentEmployees.size,
            totalWorkers = employees.size,
            dailyPayrollCost = presentEmployees.sumOf { it.dailyWage },
            activeEquipment = equipment.count { it.status == "في الخدمة" },
            totalEquipment = equipment.size,
            inventoryValue = materials.sumOf { it.quantityInStock * it.unitPrice },
            lowStockCount = materials.count { it.isLowStock },
            completedTasks = tasks.count { it.isCompleted },
            totalTasks = tasks.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())
}
