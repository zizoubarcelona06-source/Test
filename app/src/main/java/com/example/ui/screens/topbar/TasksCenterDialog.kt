package com.example.ui.screens.topbar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.localization.LocalAppStrings
import com.example.core.util.Formatters
import com.example.data.local.entity.ScheduleTaskEntity
import com.example.data.local.entity.UserAccountEntity
import com.example.ui.components.StatusChip
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusSuccess

@Composable
fun TasksCenterDialog(
    currentUser: UserAccountEntity?,
    tasks: List<ScheduleTaskEntity>,
    onToggleComplete: (ScheduleTaskEntity) -> Unit,
    onUpdateProgress: (ScheduleTaskEntity, Int) -> Unit,
    onAddTaskClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val tb = strings.topBar
    val today = remember { Formatters.todayDate() }
    var filterTab by remember { mutableIntStateOf(0) } // 0 = Mine, 1 = All, 2 = Overdue, 3 = Completed

    val filtered = remember(tasks, filterTab, currentUser?.id, today) {
        when (filterTab) {
            0 -> tasks.filter { it.assignedToUserId == currentUser?.id && !it.isCompleted }
            1 -> tasks
            2 -> tasks.filter { !it.isCompleted && it.dueDate < today }
            3 -> tasks.filter { it.isCompleted }
            else -> tasks
        }
    }

    val filterTabs = listOf(
        tb.tasksTabAssignedToMe,
        tb.tasksTabAll,
        tb.tasksTabOverdue,
        tb.tasksTabCompleted
    )

    BtpCenterDialogShell(
        title = tb.tasksCenterTitle,
        subtitle = "${tasks.count { !it.isCompleted && it.dueDate < today }} ${tb.tasksOverdueBadge}",
        onDismiss = onDismiss,
        headerActions = {
            TextButton(onClick = onAddTaskClick) {
                Icon(Icons.Default.Add, contentDescription = null, tint = AmberSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(tb.qaCreateTask, color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filterTabs.indices.toList()) { idx ->
                    FilterChip(
                        selected = filterTab == idx,
                        onClick = { filterTab = idx },
                        label = { Text(filterTabs[idx], maxLines = 1) }
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(tb.tasksEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { task ->
                        val isOverdue = !task.isCompleted && task.dueDate < today
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                1.dp,
                                if (isOverdue) StatusDanger.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Checkbox(
                                            checked = task.isCompleted,
                                            onCheckedChange = { onToggleComplete(task) }
                                        )
                                        Column {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${task.projectName} • ${task.siteName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    StatusChip(label = task.priority)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.DateRange, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "${tb.tasksStartDateLabel}: ${task.startDate}  →  ${tb.tasksDueDateLabel}: ${task.dueDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isOverdue) StatusDanger else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    if (isOverdue) {
                                        Surface(color = StatusDangerBg, shape = RoundedCornerShape(6.dp)) {
                                            Text(
                                                text = tb.tasksOverdueBadge,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = StatusDanger,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${tb.tasksAssignedToLabel}: ${task.assignedTeam}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${task.progress}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (task.isCompleted) StatusSuccess else NavyPrimary
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { (task.progress / 100f).coerceIn(0f, 1f) },
                                        color = if (isOverdue) StatusDanger else AmberSecondary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                    Slider(
                                        value = task.progress.toFloat(),
                                        onValueChange = { onUpdateProgress(task, it.toInt()) },
                                        valueRange = 0f..100f,
                                        steps = 9
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
