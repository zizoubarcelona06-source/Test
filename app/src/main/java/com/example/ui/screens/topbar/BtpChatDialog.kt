package com.example.ui.screens.topbar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.LocalAppStrings
import com.example.core.security.RolePermissions
import com.example.data.local.entity.ChatChannelEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.UserAccountEntity
import com.example.ui.components.BtpDropdownField
import com.example.ui.components.BtpInputField
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

@Composable
fun BtpChatDialog(
    currentUser: UserAccountEntity?,
    permissions: RolePermissions,
    channels: List<ChatChannelEntity>,
    selectedChannelId: Long?,
    messages: List<ChatMessageEntity>,
    filterType: String,
    searchQuery: String,
    allUsers: List<UserAccountEntity>,
    projects: List<ProjectEntity>,
    onSelectChannel: (Long?) -> Unit,
    onFilterTypeChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSendMessage: (content: String, attachmentType: String, attachmentName: String, attachmentUri: String) -> Unit,
    onStartDirectChat: (UserAccountEntity) -> Unit,
    onCreateGroupChannel: (name: String, channelType: String, projectName: String) -> Unit,
    onMarkAllRead: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val tb = strings.topBar
    var showNewDirectDialog by remember { mutableStateOf(false) }
    var showNewGroupDialog by remember { mutableStateOf(false) }

    val selectedChannel = channels.firstOrNull { it.id == selectedChannelId } ?: channels.firstOrNull()

    BtpCenterDialogShell(
        title = tb.chatTitle,
        subtitle = tb.chatSubtitle,
        onDismiss = onDismiss,
        headerActions = {
            TextButton(onClick = onMarkAllRead) {
                Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(tb.chatMarkAllRead, color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isSplitPane = maxWidth >= 640.dp

            if (isSplitPane) {
                Row(modifier = Modifier.fillMaxSize()) {
                    ChatChannelListPane(
                        channels = channels,
                        selectedChannelId = selectedChannel?.id,
                        filterType = filterType,
                        searchQuery = searchQuery,
                        permissions = permissions,
                        onFilterTypeChange = onFilterTypeChange,
                        onSearchQueryChange = onSearchQueryChange,
                        onSelectChannel = onSelectChannel,
                        onOpenNewDirect = { showNewDirectDialog = true },
                        onOpenNewGroup = { showNewGroupDialog = true },
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight()
                    )
                    HorizontalDivider(modifier = Modifier.width(1.dp).fillMaxHeight())
                    ChatMessagesPane(
                        channel = selectedChannel,
                        currentUser = currentUser,
                        permissions = permissions,
                        messages = messages,
                        showBackButton = false,
                        onBack = {},
                        onSendMessage = onSendMessage,
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                    )
                }
            } else {
                if (selectedChannelId == null || selectedChannel == null) {
                    ChatChannelListPane(
                        channels = channels,
                        selectedChannelId = null,
                        filterType = filterType,
                        searchQuery = searchQuery,
                        permissions = permissions,
                        onFilterTypeChange = onFilterTypeChange,
                        onSearchQueryChange = onSearchQueryChange,
                        onSelectChannel = onSelectChannel,
                        onOpenNewDirect = { showNewDirectDialog = true },
                        onOpenNewGroup = { showNewGroupDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    ChatMessagesPane(
                        channel = selectedChannel,
                        currentUser = currentUser,
                        permissions = permissions,
                        messages = messages,
                        showBackButton = true,
                        onBack = { onSelectChannel(null) },
                        onSendMessage = onSendMessage,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (showNewDirectDialog) {
        val colleagues = allUsers.filter { it.id != currentUser?.id && it.isActive }
        AlertDialog(
            onDismissRequest = { showNewDirectDialog = false },
            title = { Text(tb.chatNewDirectBtn, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tb.chatSelectColleague, style = MaterialTheme.typography.bodySmall)
                    colleagues.forEach { colleague ->
                        Card(
                            onClick = {
                                onStartDirectChat(colleague)
                                showNewDirectDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(color = NavyContainer, shape = CircleShape, modifier = Modifier.size(36.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(colleague.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(tb.translateRole(colleague.role), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNewDirectDialog = false }) {
                    Text(strings.close)
                }
            }
        )
    }

    if (showNewGroupDialog) {
        NewGroupChannelDialog(
            canPostAnnouncement = permissions.canPostAnnouncement,
            projectNames = projects.map { it.name },
            onSave = { name, type, projectName ->
                onCreateGroupChannel(name, type, projectName)
                showNewGroupDialog = false
            },
            onDismiss = { showNewGroupDialog = false }
        )
    }
}

@Composable
private fun ChatChannelListPane(
    channels: List<ChatChannelEntity>,
    selectedChannelId: Long?,
    filterType: String,
    searchQuery: String,
    permissions: RolePermissions,
    onFilterTypeChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectChannel: (Long) -> Unit,
    onOpenNewDirect: () -> Unit,
    onOpenNewGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tb = LocalAppStrings.current.topBar
    Column(modifier = modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(tb.chatSearchHint, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val filters = listOf(
                "ALL" to tb.chatTabAll,
                "DIRECT" to tb.chatTabDirect,
                "GROUPS" to tb.chatTabGroups,
                "ANNOUNCEMENT" to tb.chatTabAnnouncements
            )
            items(filters) { (key, label) ->
                FilterChip(
                    selected = filterType == key,
                    onClick = { onFilterTypeChange(key) },
                    label = { Text(label, maxLines = 1) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onOpenNewDirect,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(tb.chatNewDirectBtn, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
            }
            Button(
                onClick = onOpenNewGroup,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(tb.chatNewGroupBtn, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
            }
        }

        if (channels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(tb.chatNoChannels, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(channels, key = { it.id }) { ch ->
                    val isSelected = ch.id == selectedChannelId
                    val typeIcon = when (ch.channelType) {
                        "ANNOUNCEMENT" -> Icons.Default.Campaign
                        "PROJECT" -> Icons.Default.Apartment
                        "SITE" -> Icons.Default.Construction
                        else -> Icons.Default.Person
                    }
                    Card(
                        onClick = { onSelectChannel(ch.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) NavyContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) NavyPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = if (ch.channelType == "ANNOUNCEMENT") AmberContainer else NavyContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = typeIcon,
                                        contentDescription = null,
                                        tint = if (ch.channelType == "ANNOUNCEMENT") OnAmberContainer else NavyPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ch.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = formatTimestampShort(ch.lastMessageAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = ch.lastMessagePreview.ifBlank { ch.projectName },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (ch.unreadCount > 0) {
                                Surface(color = AmberSecondary, shape = CircleShape) {
                                    Text(
                                        text = "${ch.unreadCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
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

@Composable
private fun ChatMessagesPane(
    channel: ChatChannelEntity?,
    currentUser: UserAccountEntity?,
    permissions: RolePermissions,
    messages: List<ChatMessageEntity>,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onSendMessage: (content: String, attachmentType: String, attachmentName: String, attachmentUri: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tb = LocalAppStrings.current.topBar

    var draftText by remember { mutableStateOf("") }
    var pendingAttachType by remember { mutableStateOf("NONE") }
    var pendingAttachName by remember { mutableStateOf("") }
    var pendingAttachUri by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingAttachType = "IMAGE"
            pendingAttachName = resolveDisplayFileName(context, uri, "site_photo.jpg")
            pendingAttachUri = uri.toString()
        }
    }

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingAttachType = "DOCUMENT"
            pendingAttachName = resolveDisplayFileName(context, uri, "document.pdf")
            pendingAttachUri = uri.toString()
        }
    }

    Column(modifier = modifier) {
        if (channel == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(tb.chatNoChannels, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return
        }

        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showBackButton) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tb.chatBackToChannels)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (channel.projectName.isNotBlank()) {
                        Text(
                            text = channel.projectName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (channel.channelType == "ANNOUNCEMENT") {
                    Surface(color = AmberContainer, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = tb.chatAnnouncementTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnAmberContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        if (messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(tb.chatNoMessages, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isMine = msg.senderUserId == currentUser?.id
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            color = if (isMine) NavyPrimary else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            border = if (isMine) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(0.84f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${msg.senderName} • ${tb.translateRole(msg.senderRole)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMine) AmberSecondary else NavyPrimary
                                    )
                                }
                                if (msg.attachmentType == "IMAGE" && msg.attachmentUri.isNotBlank()) {
                                    UriThumbnailImage(
                                        uriString = msg.attachmentUri,
                                        contentDescription = msg.attachmentName,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                } else if (msg.attachmentType == "DOCUMENT" && msg.attachmentName.isNotBlank()) {
                                    Surface(
                                        color = if (isMine) Color.White.copy(alpha = 0.14f) else NavyContainer,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = if (isMine) Color.White else NavyPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = msg.attachmentName,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isMine) Color.White else NavyPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                if (msg.content.isNotBlank()) {
                                    Text(
                                        text = msg.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatTimestampShort(msg.timestamp),
                                        fontSize = 10.sp,
                                        color = if (isMine) Color.White.copy(alpha = 0.72f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (msg.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                                        contentDescription = if (msg.isRead) tb.chatReadLabel else tb.chatUnreadLabel,
                                        tint = if (msg.isRead) StatusSuccess else Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (msg.syncStatus == "SYNCED") Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                        contentDescription = tb.syncOpLabel(msg.syncStatus),
                                        tint = if (msg.syncStatus == "SYNCED") StatusSuccess else StatusWarning,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        val canCompose = !(channel.channelType == "ANNOUNCEMENT" && !permissions.canPostAnnouncement)
        if (!canCompose) {
            Surface(color = AmberContainer, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = tb.chatOnlyAdminsAnnouncements,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnAmberContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pendingAttachType != "NONE" && pendingAttachName.isNotBlank()) {
                    Surface(
                        color = NavyContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (pendingAttachType == "IMAGE") Icons.Default.Image else Icons.Default.Description,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = pendingAttachName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NavyPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = {
                                    pendingAttachType = "NONE"
                                    pendingAttachName = ""
                                    pendingAttachUri = ""
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(Icons.Default.Image, contentDescription = tb.chatAttachImage, tint = NavyPrimary)
                    }
                    IconButton(
                        onClick = {
                            documentPicker.launch(arrayOf("*/*"))
                        }
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = tb.chatAttachDoc, tint = NavyPrimary)
                    }
                    OutlinedTextField(
                        value = draftText,
                        onValueChange = { draftText = it },
                        placeholder = { Text(tb.chatTypeMessageHint, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = AmberSecondary,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (draftText.isNotBlank() || pendingAttachName.isNotBlank()) {
                                    onSendMessage(draftText, pendingAttachType, pendingAttachName, pendingAttachUri)
                                    draftText = ""
                                    pendingAttachType = "NONE"
                                    pendingAttachName = ""
                                    pendingAttachUri = ""
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = tb.chatSendBtn,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewGroupChannelDialog(
    canPostAnnouncement: Boolean,
    projectNames: List<String>,
    onSave: (name: String, channelType: String, projectName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val tb = strings.topBar
    var name by remember { mutableStateOf("") }
    var projectName by remember { mutableStateOf(projectNames.firstOrNull().orEmpty()) }
    var selectedType by remember { mutableStateOf("PROJECT") }

    val typeOptions = buildList {
        add("PROJECT" to tb.chatTabGroups)
        add("SITE" to strings.siteLabel)
        if (canPostAnnouncement) add("ANNOUNCEMENT" to tb.chatTabAnnouncements)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tb.chatNewConversationTitle, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BtpInputField(value = name, onValueChange = { name = it }, label = tb.chatChannelNameLabel)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    typeOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedType == key,
                            onClick = { selectedType = key },
                            label = { Text(label, maxLines = 1) }
                        )
                    }
                }
                if (projectNames.isNotEmpty()) {
                    BtpDropdownField(
                        label = tb.chatProjectOrSiteLabel,
                        selectedValue = projectName,
                        options = projectNames,
                        onOptionSelected = { projectName = it }
                    )
                } else {
                    BtpInputField(value = projectName, onValueChange = { projectName = it }, label = tb.chatProjectOrSiteLabel)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, selectedType, projectName) }
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
