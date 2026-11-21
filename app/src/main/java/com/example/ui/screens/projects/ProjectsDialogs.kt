package com.example.ui.screens.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.localization.LocalAppStrings
import com.example.core.util.Formatters
import com.example.data.local.entity.BtpConstants
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScheduleTaskEntity
import com.example.data.local.entity.SiteEntity
import com.example.ui.components.BtpDropdownField
import com.example.ui.components.BtpInputField

@Composable
fun ProjectFormDialog(
    initial: ProjectEntity?,
    onSave: (ProjectEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var code by remember { mutableStateOf(initial?.code ?: "PRJ-2026-0${(10..99).random()}") }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var clientName by remember { mutableStateOf(initial?.clientName ?: "") }
    var location by remember { mutableStateOf(initial?.location ?: "") }
    var budgetText by remember { mutableStateOf(initial?.budget?.toLong()?.toString() ?: "") }
    var spentText by remember { mutableStateOf(initial?.spentAmount?.toLong()?.toString() ?: "0") }
    var startDate by remember { mutableStateOf(initial?.startDate ?: Formatters.todayDate()) }
    var endDate by remember { mutableStateOf(initial?.endDate ?: "2027-06-30") }
    var progress by remember { mutableFloatStateOf((initial?.progressPercent ?: 0).toFloat()) }
    var status by remember { mutableStateOf(initial?.status ?: BtpConstants.projectStatuses.first()) }
    var description by remember { mutableStateOf(initial?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) strings.newProjectTitle else strings.editProjectTitle,
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
                BtpInputField(value = code, onValueChange = { code = it }, label = strings.projectCode)
                BtpInputField(value = name, onValueChange = { name = it }, label = strings.projectName)
                BtpInputField(value = clientName, onValueChange = { clientName = it }, label = strings.clientName)
                BtpInputField(value = location, onValueChange = { location = it }, label = strings.locationLabel)
                BtpInputField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = strings.estimatedBudget,
                    keyboardType = KeyboardType.Number
                )
                BtpInputField(
                    value = spentText,
                    onValueChange = { spentText = it },
                    label = strings.spentAmountLabel,
                    keyboardType = KeyboardType.Number
                )
                BtpDropdownField(
                    label = strings.projectStatus,
                    selectedValue = status,
                    options = BtpConstants.projectStatuses,
                    onOptionSelected = { status = it }
                )
                Column {
                    Text(
                        text = "${strings.progressLabel} ${progress.toInt()}%",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = progress,
                        onValueChange = { progress = it },
                        valueRange = 0f..100f
                    )
                }
                BtpInputField(value = startDate, onValueChange = { startDate = it }, label = strings.startDate)
                BtpInputField(value = endDate, onValueChange = { endDate = it }, label = strings.endDate)
                BtpInputField(
                    value = description,
                    onValueChange = { description = it },
                    label = strings.projectDesc,
                    singleLine = false
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            ProjectEntity(
                                id = initial?.id ?: 0L,
                                code = code.ifBlank { "PRJ-NEW" },
                                name = name.trim(),
                                clientName = clientName.ifBlank { "-" },
                                location = location.ifBlank { "-" },
                                budget = budgetText.toDoubleOrNull() ?: 0.0,
                                spentAmount = spentText.toDoubleOrNull() ?: 0.0,
                                startDate = startDate,
                                endDate = endDate,
                                progressPercent = progress.toInt(),
                                status = status,
                                description = description.trim()
                            )
                        )
                    }
                }
            ) {
                Text(strings.saveProject)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@Composable
fun SiteFormDialog(
    initial: SiteEntity?,
    projectNames: List<String>,
    onSave: (SiteEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var projectName by remember { mutableStateOf(initial?.projectName ?: projectNames.firstOrNull().orEmpty()) }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var supervisor by remember { mutableStateOf(initial?.supervisorName ?: "") }
    var workerCount by remember { mutableStateOf(initial?.workerCount?.toString() ?: "15") }
    var safetyStatus by remember { mutableStateOf(initial?.safetyStatus ?: BtpConstants.safetyStatuses.first()) }
    var status by remember { mutableStateOf(initial?.status ?: BtpConstants.siteStatuses.first()) }
    var dailyNote by remember { mutableStateOf(initial?.dailyNote ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) strings.newSiteTitle else strings.editSiteTitle,
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
                BtpInputField(value = name, onValueChange = { name = it }, label = strings.siteName)
                if (projectNames.isNotEmpty()) {
                    BtpDropdownField(
                        label = strings.linkedProject,
                        selectedValue = projectName,
                        options = projectNames,
                        onOptionSelected = { projectName = it }
                    )
                } else {
                    BtpInputField(value = projectName, onValueChange = { projectName = it }, label = strings.projectLabel)
                }
                BtpInputField(value = address, onValueChange = { address = it }, label = strings.addressLabel)
                BtpInputField(value = supervisor, onValueChange = { supervisor = it }, label = strings.supervisorName)
                BtpInputField(
                    value = workerCount,
                    onValueChange = { workerCount = it },
                    label = strings.workersCount,
                    keyboardType = KeyboardType.Number
                )
                BtpDropdownField(
                    label = strings.safetyStatus,
                    selectedValue = safetyStatus,
                    options = BtpConstants.safetyStatuses,
                    onOptionSelected = { safetyStatus = it }
                )
                BtpDropdownField(
                    label = strings.siteStatus,
                    selectedValue = status,
                    options = BtpConstants.siteStatuses,
                    onOptionSelected = { status = it }
                )
                BtpInputField(
                    value = dailyNote,
                    onValueChange = { dailyNote = it },
                    label = strings.dailyFieldNote,
                    singleLine = false
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            SiteEntity(
                                id = initial?.id ?: 0L,
                                projectId = initial?.projectId,
                                projectName = projectName.ifBlank { "-" },
                                name = name.trim(),
                                address = address.ifBlank { "-" },
                                supervisorName = supervisor.ifBlank { "-" },
                                workerCount = workerCount.toIntOrNull() ?: 0,
                                safetyStatus = safetyStatus,
                                status = status,
                                dailyNote = dailyNote.trim()
                            )
                        )
                    }
                }
            ) {
                Text(strings.saveSite)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@Composable
fun ScheduleTaskFormDialog(
    initial: ScheduleTaskEntity?,
    projectNames: List<String>,
    onSave: (ScheduleTaskEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var projectName by remember { mutableStateOf(initial?.projectName ?: projectNames.firstOrNull().orEmpty()) }
    var siteName by remember { mutableStateOf(initial?.siteName ?: "") }
    var assignedTeam by remember { mutableStateOf(initial?.assignedTeam ?: "") }
    var startDate by remember { mutableStateOf(initial?.startDate ?: Formatters.todayDate()) }
    var dueDate by remember { mutableStateOf(initial?.dueDate ?: Formatters.todayDate()) }
    var priority by remember { mutableStateOf(initial?.priority ?: BtpConstants.taskPriorities[1]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) strings.newTaskTitle else strings.editTaskTitle,
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
                BtpInputField(value = title, onValueChange = { title = it }, label = strings.taskTitle)
                if (projectNames.isNotEmpty()) {
                    BtpDropdownField(
                        label = strings.projectLabel,
                        selectedValue = projectName,
                        options = projectNames,
                        onOptionSelected = { projectName = it }
                    )
                } else {
                    BtpInputField(value = projectName, onValueChange = { projectName = it }, label = strings.projectLabel)
                }
                BtpInputField(value = siteName, onValueChange = { siteName = it }, label = strings.siteLabel)
                BtpInputField(value = assignedTeam, onValueChange = { assignedTeam = it }, label = strings.assignedTeam)
                BtpDropdownField(
                    label = strings.priorityLabel,
                    selectedValue = priority,
                    options = BtpConstants.taskPriorities,
                    onOptionSelected = { priority = it }
                )
                BtpInputField(value = startDate, onValueChange = { startDate = it }, label = strings.startDate)
                BtpInputField(value = dueDate, onValueChange = { dueDate = it }, label = strings.endDate)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            ScheduleTaskEntity(
                                id = initial?.id ?: 0L,
                                title = title.trim(),
                                projectName = projectName.ifBlank { "-" },
                                siteName = siteName.ifBlank { "-" },
                                assignedTeam = assignedTeam.ifBlank { "-" },
                                startDate = startDate,
                                dueDate = dueDate,
                                priority = priority,
                                isCompleted = initial?.isCompleted ?: false,
                                progress = initial?.progress ?: 0
                            )
                        )
                    }
                }
            ) {
                Text(strings.saveTask)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
