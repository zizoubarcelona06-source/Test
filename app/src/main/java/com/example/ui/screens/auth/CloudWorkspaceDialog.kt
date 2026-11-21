package com.example.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.cloudStringsFor
import com.example.data.local.entity.BtpConstants
import com.example.ui.components.BtpDropdownField
import com.example.ui.components.BtpInputField
import com.example.ui.components.BtpSegmentedTabs
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg

@Composable
fun CloudWorkspaceDialog(
    initialCompanyName: String,
    initialCurrency: String,
    currentLanguage: AppLanguage,
    isLoading: Boolean,
    errorMessage: String?,
    onCreateWorkspace: (companyName: String, currency: String, defaultInviteRole: String) -> Unit,
    onJoinWorkspace: (inviteCode: String) -> Unit,
    onSkipForNow: () -> Unit
) {
    val strings = LocalAppStrings.current
    val cs = remember(currentLanguage) { cloudStringsFor(currentLanguage) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var companyName by remember { mutableStateOf(initialCompanyName) }
    var currency by remember { mutableStateOf(initialCurrency) }
    var defaultRole by remember { mutableStateOf("مهندس موقع") }
    var inviteCodeInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onSkipForNow,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = NavyContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Business else Icons.Default.GroupAdd,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cs.workspaceSetupTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = cs.workspaceSetupSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                BtpSegmentedTabs(
                    tabs = listOf(cs.createCompanyTab, cs.joinCompanyTab),
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it }
                )

                if (!errorMessage.isNullOrBlank()) {
                    val localizedError = when (errorMessage) {
                        "ERR_INVALID_INVITE_CODE" -> cs.errInvalidInviteCode
                        "ERR_GOOGLE_SIGN_IN" -> cs.errGoogleSignInFailed
                        else -> strings.authErrorText(errorMessage)
                    }
                    Surface(
                        color = StatusDangerBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, StatusDanger.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusDanger)
                            Text(
                                text = localizedError,
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusDanger,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (selectedTab == 0) {
                    BtpInputField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = strings.companyNameLabel
                    )
                    BtpInputField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = strings.defaultCurrencyLabel
                    )
                    BtpDropdownField(
                        label = cs.defaultInviteRoleLabel,
                        selectedValue = defaultRole,
                        options = BtpConstants.userRoles,
                        optionLabel = { strings.topBar.translateRole(it) },
                        onOptionSelected = { defaultRole = it }
                    )

                    Button(
                        onClick = { onCreateWorkspace(companyName, currency, defaultRole) },
                        enabled = !isLoading && companyName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary, contentColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("create_workspace_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CloudDone, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cs.createCompanyBtn, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    BtpInputField(
                        value = inviteCodeInput,
                        onValueChange = { inviteCodeInput = it.uppercase() },
                        label = "${cs.inviteCodeLabel} (${cs.inviteCodePlaceholder})"
                    )

                    Button(
                        onClick = { onJoinWorkspace(inviteCodeInput) },
                        enabled = !isLoading && inviteCodeInput.trim().length >= 4,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("join_workspace_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.GroupAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cs.joinCompanyBtn, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                TextButton(
                    onClick = onSkipForNow,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(cs.skipOfflineForNowBtn, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
