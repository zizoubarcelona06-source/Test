package com.example.ui.screens.resources

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.localization.LocalAppStrings
import com.example.core.util.Formatters
import com.example.data.local.entity.BtpConstants
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.EquipmentEntity
import com.example.data.local.entity.MaterialEntity
import com.example.ui.components.BtpDropdownField
import com.example.ui.components.BtpInputField

@Composable
fun EmployeeFormDialog(
    initial: EmployeeEntity?,
    onSave: (EmployeeEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var fullName by remember { mutableStateOf(initial?.fullName ?: "") }
    var role by remember { mutableStateOf(initial?.role ?: BtpConstants.employeeRoles.first()) }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var nationalId by remember { mutableStateOf(initial?.nationalId ?: "") }
    var dailyWage by remember { mutableStateOf(initial?.dailyWage?.toLong()?.toString() ?: "3500") }
    var siteName by remember { mutableStateOf(initial?.assignedSiteName ?: "-") }
    var attendance by remember { mutableStateOf(initial?.attendanceStatus ?: BtpConstants.attendanceStatuses.first()) }
    var hireDate by remember { mutableStateOf(initial?.hireDate ?: Formatters.todayDate()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) strings.newEmployeeTitle else strings.editEmployeeTitle,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BtpInputField(value = fullName, onValueChange = { fullName = it }, label = strings.fullNameLabel)
                BtpDropdownField(
                    label = strings.roleLabel,
                    selectedValue = role,
                    options = BtpConstants.employeeRoles,
                    onOptionSelected = { role = it }
                )
                BtpInputField(value = phone, onValueChange = { phone = it }, label = strings.phoneLabel, keyboardType = KeyboardType.Phone)
                BtpInputField(value = nationalId, onValueChange = { nationalId = it }, label = strings.nationalIdLabel)
                BtpInputField(value = dailyWage, onValueChange = { dailyWage = it }, label = strings.dailyWageLabel, keyboardType = KeyboardType.Number)
                BtpInputField(value = siteName, onValueChange = { siteName = it }, label = strings.assignedSiteLabel)
                BtpDropdownField(
                    label = strings.attendanceStatusLabel,
                    selectedValue = attendance,
                    options = BtpConstants.attendanceStatuses,
                    onOptionSelected = { attendance = it }
                )
                BtpInputField(value = hireDate, onValueChange = { hireDate = it }, label = strings.hireDateLabel)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank()) {
                        onSave(
                            EmployeeEntity(
                                id = initial?.id ?: 0L,
                                fullName = fullName.trim(),
                                role = role,
                                phone = phone.ifBlank { "-" },
                                nationalId = nationalId.ifBlank { "-" },
                                dailyWage = dailyWage.toDoubleOrNull() ?: 0.0,
                                assignedSiteName = siteName.ifBlank { "-" },
                                attendanceStatus = attendance,
                                hireDate = hireDate,
                                isActive = true
                            )
                        )
                    }
                }
            ) { Text(strings.save) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@Composable
fun EquipmentFormDialog(
    initial: EquipmentEntity?,
    onSave: (EquipmentEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var code by remember { mutableStateOf(initial?.code ?: "EQ-${(100..999).random()}") }
    var category by remember { mutableStateOf(initial?.category ?: BtpConstants.equipmentCategories.first()) }
    var status by remember { mutableStateOf(initial?.status ?: BtpConstants.equipmentStatuses.first()) }
    var siteName by remember { mutableStateOf(initial?.assignedSiteName ?: "-") }
    var nextMaint by remember { mutableStateOf(initial?.nextMaintenanceDate ?: "2026-12-30") }
    var hourlyCost by remember { mutableStateOf(initial?.hourlyCost?.toLong()?.toString() ?: "3000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) strings.newEquipmentTitle else strings.editEquipmentTitle,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BtpInputField(value = name, onValueChange = { name = it }, label = strings.equipmentNameLabel)
                BtpInputField(value = code, onValueChange = { code = it }, label = strings.serialCodeLabel)
                BtpDropdownField(
                    label = strings.categoryLabel,
                    selectedValue = category,
                    options = BtpConstants.equipmentCategories,
                    onOptionSelected = { category = it }
                )
                BtpDropdownField(
                    label = strings.operationalStatusLabel,
                    selectedValue = status,
                    options = BtpConstants.equipmentStatuses,
                    onOptionSelected = { status = it }
                )
                BtpInputField(value = siteName, onValueChange = { siteName = it }, label = strings.siteLabel)
                BtpInputField(value = nextMaint, onValueChange = { nextMaint = it }, label = strings.nextMaintenanceLabel)
                BtpInputField(value = hourlyCost, onValueChange = { hourlyCost = it }, label = strings.hourlyCostLabel, keyboardType = KeyboardType.Number)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            EquipmentEntity(
                                id = initial?.id ?: 0L,
                                name = name.trim(),
                                code = code.ifBlank { "EQ-01" },
                                category = category,
                                status = status,
                                assignedSiteName = siteName.ifBlank { "-" },
                                nextMaintenanceDate = nextMaint,
                                hourlyCost = hourlyCost.toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                }
            ) { Text(strings.save) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@Composable
fun MaterialFormDialog(
    initial: MaterialEntity?,
    onSave: (MaterialEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var sku by remember { mutableStateOf(initial?.sku ?: "MAT-${(100..999).random()}") }
    var unit by remember { mutableStateOf(initial?.unit ?: BtpConstants.materialUnits.first()) }
    var qty by remember { mutableStateOf(initial?.quantityInStock?.toString() ?: "0") }
    var threshold by remember { mutableStateOf(initial?.minThreshold?.toString() ?: "10") }
    var unitPrice by remember { mutableStateOf(initial?.unitPrice?.toLong()?.toString() ?: "0") }
    var supplier by remember { mutableStateOf(initial?.supplierName ?: "") }
    var warehouse by remember { mutableStateOf(initial?.warehouseLocation ?: "-") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) strings.newMaterialTitle else strings.editMaterialTitle,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BtpInputField(value = name, onValueChange = { name = it }, label = strings.materialNameLabel)
                BtpInputField(value = sku, onValueChange = { sku = it }, label = strings.skuLabel)
                BtpDropdownField(
                    label = strings.unitLabel,
                    selectedValue = unit,
                    options = BtpConstants.materialUnits,
                    onOptionSelected = { unit = it }
                )
                BtpInputField(value = qty, onValueChange = { qty = it }, label = strings.currentQtyLabel, keyboardType = KeyboardType.Decimal)
                BtpInputField(value = threshold, onValueChange = { threshold = it }, label = strings.minThresholdLabel, keyboardType = KeyboardType.Decimal)
                BtpInputField(value = unitPrice, onValueChange = { unitPrice = it }, label = strings.unitPriceLabel, keyboardType = KeyboardType.Decimal)
                BtpInputField(value = supplier, onValueChange = { supplier = it }, label = strings.supplierLabel)
                BtpInputField(value = warehouse, onValueChange = { warehouse = it }, label = strings.warehouseLabel)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            MaterialEntity(
                                id = initial?.id ?: 0L,
                                name = name.trim(),
                                sku = sku.ifBlank { "MAT-00" },
                                unit = unit,
                                quantityInStock = qty.toDoubleOrNull() ?: 0.0,
                                minThreshold = threshold.toDoubleOrNull() ?: 0.0,
                                unitPrice = unitPrice.toDoubleOrNull() ?: 0.0,
                                supplierName = supplier.ifBlank { "-" },
                                warehouseLocation = warehouse.ifBlank { "-" }
                            )
                        )
                    }
                }
            ) { Text(strings.save) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
