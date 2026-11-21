package com.example.ui.screens.topbar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.core.security.RolePermissionPolicy
import com.example.core.security.RolePermissions
import com.example.data.local.entity.NotificationEntity
import com.example.ui.components.BtpSegmentedTabs
import com.example.ui.screens.dashboard.BtpDestinationTab
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusInfoBg
import com.example.ui.theme.StatusSuccess

@Composable
fun NotificationsCenterDialog(
    notifications: List<NotificationEntity>,
    preferences: Map<String, Boolean>,
    permissions: RolePermissions,
    onMarkRead: (Long) -> Unit,
    onMarkAllRead: () -> Unit,
    onTogglePreference: (String, Boolean) -> Unit,
    onNavigateFromNotification: (NotificationEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val tb = LocalAppStrings.current.topBar
    var subTab by remember { mutableIntStateOf(0) } // 0 = Inbox, 1 = Preferences
    var selectedCategory by remember { mutableStateOf("ALL") }
    var unreadOnly by remember { mutableStateOf(false) }

    val allowedCategories = remember(permissions) {
        RolePermissionPolicy.allNotificationCategories.filter { it in permissions.allowedNotificationCategories }
    }

    val displayed = remember(notifications, selectedCategory, unreadOnly) {
        notifications.filter { n ->
            (selectedCategory == "ALL" || n.category == selectedCategory) &&
                (!unreadOnly || !n.isRead)
        }
    }

    BtpCenterDialogShell(
        title = tb.notificationsTitle,
        subtitle = tb.notifRoleFilteredHint,
        onDismiss = onDismiss,
        headerActions = {
            if (subTab == 0) {
                TextButton(onClick = onMarkAllRead) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tb.notifMarkAllRead, color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BtpSegmentedTabs(
                tabs = listOf(tb.notifListTab, tb.notifPreferencesTab),
                selectedIndex = subTab,
                onSelect = { subTab = it }
            )

            if (subTab == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tb.notifUnreadOnly,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = unreadOnly,
                        onCheckedChange = { unreadOnly = it }
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategory == "ALL",
                            onClick = { selectedCategory = "ALL" },
                            label = { Text(tb.notifFilterAll, maxLines = 1) }
                        )
                    }
                    items(allowedCategories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(tb.categoryLabel(cat), maxLines = 1) }
                        )
                    }
                }

                if (displayed.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(tb.notifEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayed, key = { it.id }) { notif ->
                            Card(
                                onClick = {
                                    onMarkRead(notif.id)
                                    onNavigateFromNotification(notif)
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else AmberContainer.copy(alpha = 0.45f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (notif.isRead) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else AmberSecondary
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = if (notif.isRead) NavyContainer else AmberSecondary.copy(alpha = 0.2f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = categoryIcon(notif.category),
                                                contentDescription = null,
                                                tint = if (notif.isRead) NavyPrimary else AmberSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(color = StatusInfoBg, shape = RoundedCornerShape(6.dp)) {
                                                Text(
                                                    text = tb.categoryLabel(notif.category),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = NavyPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = formatTimestampShort(notif.createdAt),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (!notif.isRead) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(AmberSecondary, CircleShape)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = notif.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = if (notif.isRead) FontWeight.SemiBold else FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = notif.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = tb.notifPrefDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allowedCategories, key = { it }) { cat ->
                        val enabled = preferences[cat] ?: true
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTogglePreference(cat, !enabled) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = categoryIcon(cat),
                                        contentDescription = null,
                                        tint = if (enabled) NavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = tb.categoryLabel(cat),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Switch(
                                    checked = enabled,
                                    onCheckedChange = { onTogglePreference(cat, it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun categoryIcon(category: String): ImageVector = when (category) {
    "CHAT" -> Icons.Default.ChatBubble
    "ATTENDANCE" -> Icons.Default.Badge
    "LEAVE" -> Icons.Default.EventBusy
    "REWARDS" -> Icons.Default.EmojiEvents
    "EQUIPMENT" -> Icons.Default.Build
    "INVENTORY" -> Icons.Default.Inventory2
    "APPROVALS" -> Icons.Default.FactCheck
    "PROJECTS" -> Icons.Default.Apartment
    "FINANCE" -> Icons.Default.AccountBalanceWallet
    "TASKS" -> Icons.Default.TaskAlt
    "ADMIN_SECURITY" -> Icons.Default.Security
    else -> Icons.Default.Notifications
}

fun notificationTargetTab(targetTab: String): BtpDestinationTab = when (targetTab) {
    "PROJECTS" -> BtpDestinationTab.PROJECTS
    "RESOURCES" -> BtpDestinationTab.RESOURCES
    "FINANCE" -> BtpDestinationTab.FINANCE
    "REPORTS" -> BtpDestinationTab.REPORTS
    else -> BtpDestinationTab.DASHBOARD
}
