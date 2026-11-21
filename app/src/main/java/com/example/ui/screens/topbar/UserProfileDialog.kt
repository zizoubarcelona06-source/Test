package com.example.ui.screens.topbar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.data.local.entity.UserAccountEntity
import com.example.data.local.entity.UserSessionDeviceEntity
import com.example.ui.components.BtpInputField
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessBg

@Composable
fun UserProfileDialog(
    user: UserAccountEntity,
    companyName: String,
    currentLanguage: AppLanguage,
    devices: List<UserSessionDeviceEntity>,
    onUpdateProfile: (fullName: String, branchName: String, avatarUri: String) -> Unit,
    onVerifyAndChangePassword: (currentPass: String, newPass: String, (Boolean, String?) -> Unit) -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onRevokeDevice: (Long) -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val tb = strings.topBar

    var fullName by remember(user.id) { mutableStateOf(user.fullName) }
    var branchName by remember(user.id) { mutableStateOf(user.branchName) }
    var avatarUri by remember(user.id) { mutableStateOf(user.avatarUri) }
    var profileSavedMsg by remember { mutableStateOf(false) }

    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var passwordFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUri = uri.toString()
            onUpdateProfile(fullName, branchName, uri.toString())
            profileSavedMsg = true
        }
    }

    val initials = user.fullName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "BT" }

    BtpCenterDialogShell(
        title = tb.profileTitle,
        subtitle = "${tb.translateRole(user.role)} • $companyName",
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Identity Header Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (avatarUri.isNotBlank()) {
                        UriThumbnailImage(
                            uriString = avatarUri,
                            contentDescription = user.fullName,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(user.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Text("${tb.profileRoleLabel}: ${tb.translateRole(user.role)}", style = MaterialTheme.typography.bodySmall, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                        Text("${tb.profileCompanyLabel}: $companyName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = AmberContainer, shape = RoundedCornerShape(50)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AmberSecondary, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "${tb.profileRewardsPoints}: ${user.rewardPoints}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnAmberContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            avatarPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tb.profileChangePhoto)
                    }
                }
            }

            // Edit Name & Branch
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(tb.profileEditInfo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    BtpInputField(value = fullName, onValueChange = { fullName = it }, label = strings.fullNameLabel)
                    BtpInputField(value = branchName, onValueChange = { branchName = it }, label = tb.profileBranchLabel)
                    if (profileSavedMsg) {
                        Surface(color = StatusSuccessBg, shape = RoundedCornerShape(10.dp)) {
                            Text(
                                text = tb.profileUpdatedMsg,
                                style = MaterialTheme.typography.labelMedium,
                                color = StatusSuccess,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Button(
                        onClick = {
                            if (fullName.isNotBlank()) {
                                onUpdateProfile(fullName, branchName, avatarUri)
                                profileSavedMsg = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.save)
                    }
                }
            }

            // Per-User Language Card
            Card(
                onClick = onOpenLanguagePicker,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        Surface(color = NavyContainer, shape = CircleShape, modifier = Modifier.size(38.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = NavyPrimary)
                            }
                        }
                        Column {
                            Text(tb.profileLanguagePerUser, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${currentLanguage.nativeName} (${if (currentLanguage.isRtl) "RTL" else "LTR"})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(strings.edit, color = AmberSecondary, fontWeight = FontWeight.Bold)
                }
            }

            // Password Security Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = NavyPrimary)
                        Text(strings.updatePasswordTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    BtpInputField(
                        value = currentPass,
                        onValueChange = { currentPass = it },
                        label = tb.profileCurrentPassword,
                        keyboardType = KeyboardType.Password
                    )
                    BtpInputField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = strings.newPasswordLabel,
                        keyboardType = KeyboardType.Password
                    )
                    BtpInputField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = tb.profileConfirmNewPassword,
                        keyboardType = KeyboardType.Password
                    )
                    passwordFeedback?.let { (ok, msg) ->
                        Surface(
                            color = if (ok) StatusSuccessBg else StatusDangerBg,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = msg,
                                color = if (ok) StatusSuccess else StatusDanger,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            when {
                                newPass.length < 4 -> {
                                    passwordFeedback = false to strings.authErrorText("ERR_SHORT_PASSWORD")
                                }
                                newPass != confirmPass -> {
                                    passwordFeedback = false to tb.profileWrongCurrentPass
                                }
                                else -> {
                                    onVerifyAndChangePassword(currentPass, newPass) { success, errCode ->
                                        if (success) {
                                            currentPass = ""
                                            newPass = ""
                                            confirmPass = ""
                                            passwordFeedback = true to strings.authErrorText("MSG_PASSWORD_UPDATED")
                                        } else {
                                            val errMsg = if (errCode == "ERR_WRONG_CURRENT_PASSWORD") tb.profileWrongCurrentPass
                                            else strings.authErrorText(errCode ?: "ERR_GENERIC_AUTH")
                                            passwordFeedback = false to errMsg
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.changePasswordBtn)
                    }
                }
            }

            // Active Devices & Sessions
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Devices, contentDescription = null, tint = NavyPrimary)
                        Text(tb.profileDevicesTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    devices.forEach { dev ->
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(dev.deviceName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    if (dev.isCurrentDevice) {
                                        Surface(color = StatusSuccessBg, shape = RoundedCornerShape(6.dp)) {
                                            Text(
                                                text = tb.profileCurrentDeviceBadge,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = StatusSuccess,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${dev.deviceModel} • ${dev.locationLabel} • ${tb.profileLastActive}: ${formatTimestampShort(dev.lastActiveAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!dev.isCurrentDevice) {
                                if (dev.isActive) {
                                    TextButton(onClick = { onRevokeDevice(dev.id) }) {
                                        Text(tb.profileRevokeSession, color = StatusDanger, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Surface(color = StatusDangerBg, shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = tb.profileSessionRevokedBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusDanger,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onDismiss()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusDanger),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(tb.profileLogoutBtn, fontWeight = FontWeight.Bold)
            }
        }
    }
}
