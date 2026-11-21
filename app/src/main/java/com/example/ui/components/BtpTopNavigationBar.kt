package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.sync.SyncState
import com.example.core.sync.SyncStatusSnapshot
import com.example.data.local.entity.UserAccountEntity
import com.example.ui.screens.topbar.UriThumbnailImage
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavyPrimaryDark
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess

@Composable
fun BtpTopNavigationBar(
    currentUser: UserAccountEntity?,
    companyName: String,
    sectionTitle: String,
    currentLanguage: AppLanguage,
    unreadChatCount: Int,
    unreadNotificationsCount: Int,
    pendingTasksCount: Int,
    syncSnapshot: SyncStatusSnapshot,
    onOpenChat: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenGlobalSearch: () -> Unit,
    onOpenQuickActions: () -> Unit,
    onOpenTasksCenter: () -> Unit,
    onOpenSyncStatus: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onLogout: () -> Unit
) {
    val strings = LocalAppStrings.current
    val tb = strings.topBar

    Surface(
        color = NavyPrimary,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                val isWide = maxWidth >= 700.dp

                if (isWide) {
                    // Single executive row on Tablets & Desktops
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BrandIdentityCluster(
                            sectionTitle = sectionTitle,
                            companyName = companyName,
                            modifier = Modifier.weight(1.1f)
                        )

                        GlobalSearchTriggerPill(
                            hint = tb.globalSearchTitle,
                            onClick = onOpenGlobalSearch,
                            modifier = Modifier
                                .weight(1.0f)
                                .testTag("topbar_search_button")
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SyncStatusPillButton(
                                snapshot = syncSnapshot,
                                onClick = onOpenSyncStatus,
                                compact = false
                            )

                            QuickActionPillButton(
                                label = tb.quickActionsTitle,
                                onClick = onOpenQuickActions
                            )

                            TopBarBadgeIconButton(
                                icon = Icons.Default.TaskAlt,
                                contentDescription = tb.tasksCenterTitle,
                                badgeCount = pendingTasksCount,
                                badgeColor = AmberSecondary,
                                onClick = onOpenTasksCenter,
                                tag = "topbar_tasks_button"
                            )

                            TopBarBadgeIconButton(
                                icon = Icons.Default.ChatBubbleOutline,
                                contentDescription = tb.chatTitle,
                                badgeCount = unreadChatCount,
                                badgeColor = AmberSecondary,
                                onClick = onOpenChat,
                                tag = "topbar_chat_button"
                            )

                            TopBarBadgeIconButton(
                                icon = Icons.Default.NotificationsNone,
                                contentDescription = tb.notificationsTitle,
                                badgeCount = unreadNotificationsCount,
                                badgeColor = StatusDanger,
                                onClick = onOpenNotifications,
                                tag = "topbar_notifications_button"
                            )

                            LanguageChipButton(
                                language = currentLanguage,
                                contentDescription = strings.chooseLanguage,
                                onClick = onOpenLanguagePicker
                            )

                            UserProfileChip(
                                user = currentUser,
                                roleTranslated = tb.translateRole(currentUser?.role.orEmpty()),
                                onClick = onOpenProfile,
                                showName = true
                            )

                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier.testTag("dashboard_lock_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = strings.lockAndLogoutDesc,
                                    tint = Color.White.copy(alpha = 0.88f)
                                )
                            }
                        }
                    }
                } else {
                    // Clean two-row ergonomic layout on Phones
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BrandIdentityCluster(
                                sectionTitle = sectionTitle,
                                companyName = companyName,
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SyncStatusPillButton(
                                    snapshot = syncSnapshot,
                                    onClick = onOpenSyncStatus,
                                    compact = true
                                )
                                LanguageChipButton(
                                    language = currentLanguage,
                                    contentDescription = strings.chooseLanguage,
                                    onClick = onOpenLanguagePicker
                                )
                                UserProfileChip(
                                    user = currentUser,
                                    roleTranslated = tb.translateRole(currentUser?.role.orEmpty()),
                                    onClick = onOpenProfile,
                                    showName = false
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GlobalSearchTriggerPill(
                                hint = tb.globalSearchTitle,
                                onClick = onOpenGlobalSearch,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("topbar_search_button")
                            )

                            QuickActionPillButton(
                                label = null,
                                onClick = onOpenQuickActions
                            )

                            TopBarBadgeIconButton(
                                icon = Icons.Default.TaskAlt,
                                contentDescription = tb.tasksCenterTitle,
                                badgeCount = pendingTasksCount,
                                badgeColor = AmberSecondary,
                                onClick = onOpenTasksCenter,
                                tag = "topbar_tasks_button"
                            )

                            TopBarBadgeIconButton(
                                icon = Icons.Default.ChatBubbleOutline,
                                contentDescription = tb.chatTitle,
                                badgeCount = unreadChatCount,
                                badgeColor = AmberSecondary,
                                onClick = onOpenChat,
                                tag = "topbar_chat_button"
                            )

                            TopBarBadgeIconButton(
                                icon = Icons.Default.NotificationsNone,
                                contentDescription = tb.notificationsTitle,
                                badgeCount = unreadNotificationsCount,
                                badgeColor = StatusDanger,
                                onClick = onOpenNotifications,
                                tag = "topbar_notifications_button"
                            )
                        }
                    }
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
}

@Composable
private fun BrandIdentityCluster(
    sectionTitle: String,
    companyName: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        BtpBrandEmblem(size = 36.dp, onDarkBackground = true, showBadgeFrame = false)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "BTP Manager",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = sectionTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberBright,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = companyName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GlobalSearchTriggerPill(
    hint: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NavyPrimaryDark.copy(alpha = 0.65f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = hint,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionPillButton(
    label: String?,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    Surface(
        color = AmberSecondary,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("topbar_quick_actions_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (label == null) 10.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = strings.topBar.quickActionsTitle,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            if (!label.isNullOrBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TopBarBadgeIconButton(
    icon: ImageVector,
    contentDescription: String,
    badgeCount: Int,
    badgeColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    Box(contentAlignment = Alignment.Center) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(44.dp)
                .testTag(tag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        if (badgeCount > 0) {
            Surface(
                color = badgeColor,
                shape = CircleShape,
                border = BorderStroke(1.dp, NavyPrimary),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 3.dp, end = 3.dp)
                    .size(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (badgeCount > 99) "99+" else "$badgeCount",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncStatusPillButton(
    snapshot: SyncStatusSnapshot,
    onClick: () -> Unit,
    compact: Boolean
) {
    val tb = LocalAppStrings.current.topBar
    val (color, icon, label) = when (snapshot.syncState) {
        SyncState.ONLINE -> Triple(StatusSuccess, Icons.Default.CheckCircle, tb.syncOnline)
        SyncState.OFFLINE -> Triple(AmberSecondary, Icons.Default.CloudOff, tb.syncOffline)
        SyncState.SYNCHRONIZING -> Triple(StatusInfo, Icons.Default.CloudSync, tb.syncSynchronizing)
        SyncState.SYNC_ERROR -> Triple(StatusDanger, Icons.Default.ErrorOutline, tb.syncError)
    }

    Surface(
        color = color.copy(alpha = 0.22f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color.copy(alpha = 0.7f)),
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("topbar_sync_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 8.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            if (!compact) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
            if (snapshot.pendingCount > 0 || snapshot.errorCount > 0) {
                Surface(
                    color = color,
                    shape = CircleShape
                ) {
                    Text(
                        text = "${snapshot.pendingCount + snapshot.errorCount}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageChipButton(
    language: AppLanguage,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("dashboard_language_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = contentDescription,
                tint = AmberBright,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = language.code.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun UserProfileChip(
    user: UserAccountEntity?,
    roleTranslated: String,
    onClick: () -> Unit,
    showName: Boolean
) {
    val tb = LocalAppStrings.current.topBar
    val initials = user?.fullName
        ?.split(" ")
        ?.filter { it.isNotBlank() }
        ?.take(2)
        ?.joinToString("") { it.first().uppercase() }
        ?.ifBlank { "BT" } ?: "BT"

    Surface(
        color = Color.White.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, AmberBright.copy(alpha = 0.45f)),
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("topbar_profile_button")
    ) {
        Row(
            modifier = Modifier.padding(
                start = 4.dp,
                end = if (showName) 10.dp else 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!user?.avatarUri.isNullOrBlank()) {
                UriThumbnailImage(
                    uriString = user!!.avatarUri,
                    contentDescription = tb.profileTitle,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(AmberSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
            if (showName && user != null) {
                Column {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = roleTranslated,
                        fontSize = 9.sp,
                        color = AmberBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
