package com.example.ui.screens.topbar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.localization.LocalAppStrings
import com.example.core.security.RolePermissions
import com.example.ui.components.BtpDropdownField
import com.example.ui.components.BtpInputField
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg

enum class QuickActionType {
    ADD_WORKER,
    ADD_PROJECT,
    RECORD_EXPENSE,
    ADD_EQUIPMENT,
    REPORT_FAULT,
    SEND_MESSAGE,
    CREATE_TASK
}

private data class QuickActionItem(
    val type: QuickActionType,
    val label: String,
    val icon: ImageVector,
    val accent: Color,
    val enabled: Boolean
)

@Composable
fun QuickActionsDialog(
    permissions: RolePermissions,
    onTriggerAction: (QuickActionType) -> Unit,
    onDismiss: () -> Unit
) {
    val tb = LocalAppStrings.current.topBar

    val actions = listOf(
        QuickActionItem(QuickActionType.ADD_WORKER, tb.qaAddWorker, Icons.Default.PersonAdd, NavyPrimary, permissions.canAddEmployee),
        QuickActionItem(QuickActionType.ADD_PROJECT, tb.qaAddProject, Icons.Default.Apartment, NavyPrimary, permissions.canAddProject),
        QuickActionItem(QuickActionType.RECORD_EXPENSE, tb.qaRecordExpense, Icons.Default.AccountBalanceWallet, AmberSecondary, permissions.canAddExpense),
        QuickActionItem(QuickActionType.ADD_EQUIPMENT, tb.qaAddEquipment, Icons.Default.Build, NavyPrimary, permissions.canAddEquipment),
        QuickActionItem(QuickActionType.REPORT_FAULT, tb.qaReportFault, Icons.Default.ReportProblem, StatusDanger, permissions.canReportFault),
        QuickActionItem(QuickActionType.SEND_MESSAGE, tb.qaSendMessage, Icons.Default.ChatBubble, AmberSecondary, permissions.canSendMessage),
        QuickActionItem(QuickActionType.CREATE_TASK, tb.qaCreateTask, Icons.Default.TaskAlt, NavyPrimary, permissions.canCreateTask)
    )

    BtpCenterDialogShell(
        title = tb.quickActionsTitle,
        subtitle = tb.quickActionsSubtitle,
        onDismiss = onDismiss,
        fillHeightFraction = 0.72f
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 155.dp),
            contentPadding = PaddingValues(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(actions) { item ->
                Card(
                    onClick = { if (item.enabled) onTriggerAction(item.type) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (item.enabled) item.accent.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (item.enabled) item.accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (item.enabled) item.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            if (!item.enabled) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = tb.qaPermissionRestricted,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (item.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!item.enabled) {
                                Text(
                                    text = tb.qaPermissionRestricted,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusDanger
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FaultReportDialog(
    equipmentNames: List<String>,
    siteNames: List<String>,
    onSubmit: (equipmentName: String, siteName: String, severity: String, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val tb = strings.topBar

    var equipmentName by remember { mutableStateOf(equipmentNames.firstOrNull().orEmpty()) }
    var siteName by remember { mutableStateOf(siteNames.firstOrNull().orEmpty()) }
    var severity by remember { mutableStateOf("عالية") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tb.faultReportTitle, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (equipmentNames.isNotEmpty()) {
                    BtpDropdownField(
                        label = tb.faultEquipmentLabel,
                        selectedValue = equipmentName,
                        options = equipmentNames,
                        onOptionSelected = { equipmentName = it }
                    )
                } else {
                    BtpInputField(value = equipmentName, onValueChange = { equipmentName = it }, label = tb.faultEquipmentLabel)
                }
                if (siteNames.isNotEmpty()) {
                    BtpDropdownField(
                        label = tb.faultSiteLabel,
                        selectedValue = siteName,
                        options = siteNames,
                        onOptionSelected = { siteName = it }
                    )
                } else {
                    BtpInputField(value = siteName, onValueChange = { siteName = it }, label = tb.faultSiteLabel)
                }
                BtpDropdownField(
                    label = tb.faultSeverityLabel,
                    selectedValue = severity,
                    options = listOf("عالية", "متوسطة", "منخفضة"),
                    onOptionSelected = { severity = it }
                )
                BtpInputField(
                    value = description,
                    onValueChange = { description = it },
                    label = tb.faultDescriptionLabel,
                    singleLine = false
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (equipmentName.isNotBlank() && description.isNotBlank()) {
                        onSubmit(equipmentName, siteName, severity, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
            ) {
                Text(tb.faultSubmitBtn, color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
