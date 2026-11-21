package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BtpConstants
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.EquipmentEntity
import com.example.data.local.entity.MaterialEntity
import com.example.data.repository.BtpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ResourcesUiState(
    val employees: List<EmployeeEntity> = emptyList(),
    val equipment: List<EquipmentEntity> = emptyList(),
    val materials: List<MaterialEntity> = emptyList(),
    val searchQuery: String = "",
    val showLowStockOnly: Boolean = false
)

class ResourcesViewModel(private val repository: BtpRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val showLowStockOnly = MutableStateFlow(false)

    val uiState: StateFlow<ResourcesUiState> = combine(
        repository.employees,
        repository.equipment,
        repository.materials,
        searchQuery,
        showLowStockOnly
    ) { employees, equipment, materials, query, lowOnly ->
        val q = query.trim().lowercase()
        ResourcesUiState(
            employees = employees.filter {
                q.isEmpty() || it.fullName.lowercase().contains(q) || it.role.lowercase().contains(q) || it.assignedSiteName.lowercase().contains(q)
            },
            equipment = equipment.filter {
                q.isEmpty() || it.name.lowercase().contains(q) || it.code.lowercase().contains(q) || it.category.lowercase().contains(q)
            },
            materials = materials.filter {
                (!lowOnly || it.isLowStock) &&
                    (q.isEmpty() || it.name.lowercase().contains(q) || it.sku.lowercase().contains(q) || it.supplierName.lowercase().contains(q))
            },
            searchQuery = query,
            showLowStockOnly = lowOnly
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResourcesUiState())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun toggleLowStockOnly() {
        showLowStockOnly.value = !showLowStockOnly.value
    }

    fun cycleEmployeeAttendance(employee: EmployeeEntity) {
        viewModelScope.launch {
            val statuses = BtpConstants.attendanceStatuses
            val nextIdx = (statuses.indexOf(employee.attendanceStatus) + 1) % statuses.size
            repository.saveEmployee(employee.copy(attendanceStatus = statuses[nextIdx]))
        }
    }

    fun saveEmployee(employee: EmployeeEntity) {
        viewModelScope.launch { repository.saveEmployee(employee) }
    }

    fun deleteEmployee(employee: EmployeeEntity) {
        viewModelScope.launch { repository.deleteEmployee(employee) }
    }

    fun cycleEquipmentStatus(item: EquipmentEntity) {
        viewModelScope.launch {
            val statuses = BtpConstants.equipmentStatuses
            val nextIdx = (statuses.indexOf(item.status) + 1) % statuses.size
            repository.saveEquipment(item.copy(status = statuses[nextIdx]))
        }
    }

    fun saveEquipment(item: EquipmentEntity) {
        viewModelScope.launch { repository.saveEquipment(item) }
    }

    fun deleteEquipment(item: EquipmentEntity) {
        viewModelScope.launch { repository.deleteEquipment(item) }
    }

    fun adjustMaterialStock(material: MaterialEntity, delta: Double) {
        viewModelScope.launch {
            val updated = (material.quantityInStock + delta).coerceAtLeast(0.0)
            repository.saveMaterial(material.copy(quantityInStock = updated))
        }
    }

    fun saveMaterial(material: MaterialEntity) {
        viewModelScope.launch { repository.saveMaterial(material) }
    }

    fun deleteMaterial(material: MaterialEntity) {
        viewModelScope.launch { repository.deleteMaterial(material) }
    }
}
