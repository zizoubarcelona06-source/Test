package com.example.ui.screens.resources

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.localization.LocalAppStrings
import com.example.core.util.Formatters
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.EquipmentEntity
import com.example.data.local.entity.MaterialEntity
import com.example.ui.components.BtpBrandEmblem
import com.example.ui.components.BtpSegmentedTabs
import com.example.ui.components.BtpUnifiedModuleIcon
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ItemActionsRow
import com.example.ui.components.SearchAndFilterRow
import com.example.ui.components.StatusChip
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.viewmodel.ResourcesUiState

@Composable
fun ResourcesHubScreen(
    uiState: ResourcesUiState,
    selectedSubTab: Int,
    onSubTabChange: (Int) -> Unit,
    currency: String,
    onSearchChange: (String) -> Unit,
    onToggleLowStockOnly: () -> Unit,
    onCycleEmployeeAttendance: (EmployeeEntity) -> Unit,
    onSaveEmployee: (EmployeeEntity) -> Unit,
    onDeleteEmployee: (EmployeeEntity) -> Unit,
    onCycleEquipmentStatus: (EquipmentEntity) -> Unit,
    onSaveEquipment: (EquipmentEntity) -> Unit,
    onDeleteEquipment: (EquipmentEntity) -> Unit,
    onAdjustMaterialStock: (MaterialEntity, Double) -> Unit,
    onSaveMaterial: (MaterialEntity) -> Unit,
    onDeleteMaterial: (MaterialEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    var editingEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }
    var showEmployeeDialog by remember { mutableStateOf(false) }
    var deletingEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }

    var editingEquipment by remember { mutableStateOf<EquipmentEntity?>(null) }
    var showEquipmentDialog by remember { mutableStateOf(false) }
    var deletingEquipment by remember { mutableStateOf<EquipmentEntity?>(null) }

    var editingMaterial by remember { mutableStateOf<MaterialEntity?>(null) }
    var showMaterialDialog by remember { mutableStateOf(false) }
    var deletingMaterial by remember { mutableStateOf<MaterialEntity?>(null) }

    val activeCount = when (selectedSubTab) {
        0 -> uiState.employees.size
        1 -> uiState.equipment.size
        else -> uiState.materials.size
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedSubTab) {
                        0 -> {
                            editingEmployee = null
                            showEmployeeDialog = true
                        }
                        1 -> {
                            editingEquipment = null
                            showEquipmentDialog = true
                        }
                        else -> {
                            editingMaterial = null
                            showMaterialDialog = true
                        }
                    }
                },
                containerColor = AmberSecondary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("resources_fab_add")
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
            Surface(
                color = NavyPrimary,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            BtpBrandEmblem(
                                size = 36.dp,
                                onDarkBackground = true,
                                showBadgeFrame = false
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.resourcesHubTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            color = AmberSecondary.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, AmberBright.copy(alpha = 0.55f))
                        ) {
                            Text(
                                text = "$activeCount",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(AmberSecondary)
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
                tabs = listOf(strings.tabEmployees, strings.tabEquipment, strings.tabMaterials),
                selectedIndex = selectedSubTab,
                onSelect = onSubTabChange
            )

            SearchAndFilterRow(
                query = uiState.searchQuery,
                onQueryChange = onSearchChange,
                placeholder = when (selectedSubTab) {
                    0 -> strings.searchEmployeesHint
                    1 -> strings.searchEquipmentHint
                    else -> strings.searchMaterialsHint
                }
            )

            if (selectedSubTab == 2) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = uiState.showLowStockOnly,
                        onClick = onToggleLowStockOnly,
                        label = { Text(strings.lowStockFilterChip) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusDanger,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val twoColumns = maxWidth >= 680.dp

                when (selectedSubTab) {
                    0 -> {
                        if (uiState.employees.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Outlined.Badge,
                                title = strings.emptyEmployeesTitle,
                                subtitle = strings.emptyEmployeesSub,
                                actionLabel = strings.addEmployee,
                                onAction = {
                                    editingEmployee = null
                                    showEmployeeDialog = true
                                }
                            )
                        } else if (twoColumns) {
                            val rows = uiState.employees.chunked(2)
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(rows) { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { emp ->
                                            EmployeeCard(
                                                employee = emp,
                                                currency = currency,
                                                onCycleAttendance = { onCycleEmployeeAttendance(emp) },
                                                onEdit = {
                                                    editingEmployee = emp
                                                    showEmployeeDialog = true
                                                },
                                                onDelete = { deletingEmployee = emp },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.employees, key = { it.id }) { emp ->
                                    EmployeeCard(
                                        employee = emp,
                                        currency = currency,
                                        onCycleAttendance = { onCycleEmployeeAttendance(emp) },
                                        onEdit = {
                                            editingEmployee = emp
                                            showEmployeeDialog = true
                                        },
                                        onDelete = { deletingEmployee = emp }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        if (uiState.equipment.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Outlined.PrecisionManufacturing,
                                title = strings.emptyEquipmentTitle,
                                subtitle = strings.emptyEquipmentSub,
                                actionLabel = strings.addEquipment,
                                onAction = {
                                    editingEquipment = null
                                    showEquipmentDialog = true
                                }
                            )
                        } else if (twoColumns) {
                            val rows = uiState.equipment.chunked(2)
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(rows) { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { eq ->
                                            EquipmentCard(
                                                equipment = eq,
                                                currency = currency,
                                                onCycleStatus = { onCycleEquipmentStatus(eq) },
                                                onEdit = {
                                                    editingEquipment = eq
                                                    showEquipmentDialog = true
                                                },
                                                onDelete = { deletingEquipment = eq },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.equipment, key = { it.id }) { eq ->
                                    EquipmentCard(
                                        equipment = eq,
                                        currency = currency,
                                        onCycleStatus = { onCycleEquipmentStatus(eq) },
                                        onEdit = {
                                            editingEquipment = eq
                                            showEquipmentDialog = true
                                        },
                                        onDelete = { deletingEquipment = eq }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        if (uiState.materials.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Outlined.Inventory2,
                                title = strings.emptyMaterialsTitle,
                                subtitle = strings.emptyMaterialsSub,
                                actionLabel = strings.addMaterial,
                                onAction = {
                                    editingMaterial = null
                                    showMaterialDialog = true
                                }
                            )
                        } else if (twoColumns) {
                            val rows = uiState.materials.chunked(2)
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(rows) { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { mat ->
                                            MaterialCard(
                                                material = mat,
                                                currency = currency,
                                                onIncrement = { onAdjustMaterialStock(mat, 1.0) },
                                                onDecrement = { onAdjustMaterialStock(mat, -1.0) },
                                                onEdit = {
                                                    editingMaterial = mat
                                                    showMaterialDialog = true
                                                },
                                                onDelete = { deletingMaterial = mat },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.materials, key = { it.id }) { mat ->
                                    MaterialCard(
                                        material = mat,
                                        currency = currency,
                                        onIncrement = { onAdjustMaterialStock(mat, 1.0) },
                                        onDecrement = { onAdjustMaterialStock(mat, -1.0) },
                                        onEdit = {
                                            editingMaterial = mat
                                            showMaterialDialog = true
                                        },
                                        onDelete = { deletingMaterial = mat }
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

    if (showEmployeeDialog) {
        EmployeeFormDialog(
            initial = editingEmployee,
            onSave = {
                onSaveEmployee(it)
                showEmployeeDialog = false
            },
            onDismiss = { showEmployeeDialog = false }
        )
    }

    if (showEquipmentDialog) {
        EquipmentFormDialog(
            initial = editingEquipment,
            onSave = {
                onSaveEquipment(it)
                showEquipmentDialog = false
            },
            onDismiss = { showEquipmentDialog = false }
        )
    }

    if (showMaterialDialog) {
        MaterialFormDialog(
            initial = editingMaterial,
            onSave = {
                onSaveMaterial(it)
                showMaterialDialog = false
            },
            onDismiss = { showMaterialDialog = false }
        )
    }

    deletingEmployee?.let { e ->
        ConfirmDeleteDialog(
            title = strings.deleteEmployeeTitle,
            message = "${strings.confirmDeletePrefix} \"${e.fullName}\"؟",
            onConfirm = {
                onDeleteEmployee(e)
                deletingEmployee = null
            },
            onDismiss = { deletingEmployee = null }
        )
    }

    deletingEquipment?.let { eq ->
        ConfirmDeleteDialog(
            title = strings.deleteEquipmentTitle,
            message = "${strings.confirmDeletePrefix} \"${eq.name}\"؟",
            onConfirm = {
                onDeleteEquipment(eq)
                deletingEquipment = null
            },
            onDismiss = { deletingEquipment = null }
        )
    }

    deletingMaterial?.let { m ->
        ConfirmDeleteDialog(
            title = strings.deleteMaterialTitle,
            message = "${strings.confirmDeletePrefix} \"${m.name}\"؟",
            onConfirm = {
                onDeleteMaterial(m)
                deletingMaterial = null
            },
            onDismiss = { deletingMaterial = null }
        )
    }
}

@Composable
private fun EmployeeCard(
    employee: EmployeeEntity,
    currency: String,
    onCycleAttendance: () -> Unit,
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
                        icon = Icons.Outlined.Badge,
                        isAccentOrange = false,
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = employee.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${employee.role} • ${employee.assignedSiteName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                StatusChip(label = employee.attendanceStatus, onClick = onCycleAttendance)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(employee.phone, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = "${strings.dailyWageHint} ${Formatters.formatMoney(employee.dailyWage, currency)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberSecondary
                    )
                }
                ItemActionsRow(onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun EquipmentCard(
    equipment: EquipmentEntity,
    currency: String,
    onCycleStatus: () -> Unit,
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
                        icon = Icons.Outlined.PrecisionManufacturing,
                        isAccentOrange = true,
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(equipment.code, style = MaterialTheme.typography.labelSmall, color = AmberSecondary, fontWeight = FontWeight.Bold)
                        Text(
                            text = equipment.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${equipment.category} • ${equipment.assignedSiteName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                StatusChip(label = equipment.status, onClick = onCycleStatus)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${strings.nextMaintenanceLabel} ${equipment.nextMaintenanceDate}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${strings.hourlyCostLabel} ${Formatters.formatMoney(equipment.hourlyCost, currency)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                ItemActionsRow(onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun MaterialCard(
    material: MaterialEntity,
    currency: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (material.isLowStock) StatusDanger.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        icon = Icons.Outlined.Inventory2,
                        isAccentOrange = false,
                        isAlert = material.isLowStock,
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(material.sku, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = material.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${material.supplierName} • ${material.warehouseLocation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (material.isLowStock) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(color = StatusDangerBg, shape = RoundedCornerShape(50)) {
                        Text(
                            text = strings.lowStockBadge,
                            color = StatusDanger,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDecrement, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "-", modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = Formatters.formatQuantity(material.quantityInStock, material.unit),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(onClick = onIncrement, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "+", modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${Formatters.formatMoney(material.unitPrice, currency)} / ${material.unit}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberSecondary
                    )
                    ItemActionsRow(onEdit = onEdit, onDelete = onDelete)
                }
            }
        }
    }
}
