package com.example.ui.screens.reports

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AddModerator
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.security.AppPreferences
import com.example.core.util.Formatters
import com.example.data.local.entity.BtpConstants
import com.example.data.local.entity.UserAccountEntity
import com.example.ui.components.BtpBrandEmblem
import com.example.ui.components.BtpDropdownField
import com.example.ui.components.BtpInputField
import com.example.ui.components.BtpSegmentedTabs
import com.example.ui.components.BtpUnifiedModuleIcon
import com.example.ui.components.DonutBreakdownChart
import com.example.ui.components.LanguagePickerDialog
import com.example.ui.components.ProjectBudgetBarsChart
import com.example.ui.components.StatKpiCard
import com.example.ui.components.StatusChip
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.SteelTertiary
import com.example.viewmodel.AuthUiState
import com.example.viewmodel.ReportsUiState

@Composable
fun ReportsAndSecurityScreen(
    reportsState: ReportsUiState,
    authState: AuthUiState,
    preferences: AppPreferences,
    allUsers: List<UserAccountEntity>,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    selectedSubTab: Int,
    onSubTabChange: (Int) -> Unit,
    onCreateUser: (fullName: String, username: String, phone: String, role: String, password: String) -> Unit,
    onChangePassword: (String) -> Unit,
    onToggleUserActive: (UserAccountEntity) -> Unit,
    onSetDarkThemeMode: (Boolean?) -> Unit,
    onUpdateCompanySettings: (companyName: String, currency: String) -> Unit,
    onResetSampleData: () -> Unit,
    onLogout: () -> Unit,
    onClearMessages: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showCompanySettingsDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BtpBrandEmblem(
                        size = 36.dp,
                        onDarkBackground = true,
                        showBadgeFrame = false
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.reportsHubTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                tabs = listOf(strings.tabReports, strings.tabSecurity),
                selectedIndex = selectedSubTab,
                onSelect = onSubTabChange
            )

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val isWideTablet = maxWidth >= 700.dp

            if (selectedSubTab == 0) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        if (isWideTablet) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatKpiCard(
                                    title = strings.avgProjectProgress,
                                    value = "${reportsState.averageProjectProgress}%",
                                    subtitle = "${reportsState.totalProjects} ${strings.registeredProjectsSub}",
                                    icon = Icons.Outlined.Apartment,
                                    accentColor = NavyPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                StatKpiCard(
                                    title = strings.currentInventoryValue,
                                    value = Formatters.formatMoney(reportsState.inventoryValue, preferences.currencySymbol),
                                    subtitle = "${strings.lowStockCountSub} ${reportsState.lowStockCount}",
                                    icon = Icons.Outlined.Inventory2,
                                    accentColor = SteelTertiary,
                                    isAlert = reportsState.lowStockCount > 0,
                                    modifier = Modifier.weight(1f)
                                )
                                StatKpiCard(
                                    title = strings.dailyPayrollCost,
                                    value = Formatters.formatMoney(reportsState.dailyPayrollCost, preferences.currencySymbol),
                                    subtitle = "${strings.presentWorkersSub} ${reportsState.presentWorkers}/${reportsState.totalWorkers}",
                                    icon = Icons.Outlined.Groups,
                                    accentColor = StatusSuccess,
                                    modifier = Modifier.weight(1f)
                                )
                                StatKpiCard(
                                    title = strings.equipmentReadiness,
                                    value = "${reportsState.activeEquipment}/${reportsState.totalEquipment}",
                                    subtitle = "${strings.completedTasksSub} ${reportsState.completedTasks}/${reportsState.totalTasks}",
                                    icon = Icons.Outlined.PrecisionManufacturing,
                                    accentColor = AmberSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    StatKpiCard(
                                        title = strings.avgProjectProgress,
                                        value = "${reportsState.averageProjectProgress}%",
                                        subtitle = "${reportsState.totalProjects} ${strings.registeredProjectsSub}",
                                        icon = Icons.Outlined.Apartment,
                                        accentColor = NavyPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatKpiCard(
                                        title = strings.currentInventoryValue,
                                        value = Formatters.formatMoney(reportsState.inventoryValue, preferences.currencySymbol),
                                        subtitle = "${strings.lowStockCountSub} ${reportsState.lowStockCount}",
                                        icon = Icons.Outlined.Inventory2,
                                        accentColor = SteelTertiary,
                                        isAlert = reportsState.lowStockCount > 0,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    StatKpiCard(
                                        title = strings.dailyPayrollCost,
                                        value = Formatters.formatMoney(reportsState.dailyPayrollCost, preferences.currencySymbol),
                                        subtitle = "${strings.presentWorkersSub} ${reportsState.presentWorkers}/${reportsState.totalWorkers}",
                                        icon = Icons.Outlined.Groups,
                                        accentColor = StatusSuccess,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatKpiCard(
                                        title = strings.equipmentReadiness,
                                        value = "${reportsState.activeEquipment}/${reportsState.totalEquipment}",
                                        subtitle = "${strings.completedTasksSub} ${reportsState.completedTasks}/${reportsState.totalTasks}",
                                        icon = Icons.Outlined.PrecisionManufacturing,
                                        accentColor = AmberSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    if (isWideTablet) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            text = strings.expensesByCategoryTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        DonutBreakdownChart(
                                            items = reportsState.expensesByCategory,
                                            currency = preferences.currencySymbol
                                        )
                                    }
                                }
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            text = strings.budgetVsSpentTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (reportsState.projectBudgetStats.isEmpty()) {
                                            Text(strings.noProjectsChartData, style = MaterialTheme.typography.bodySmall)
                                        } else {
                                            ProjectBudgetBarsChart(
                                                stats = reportsState.projectBudgetStats,
                                                currency = preferences.currencySymbol
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Donut Chart: Expenses Breakdown
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = strings.expensesByCategoryTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    DonutBreakdownChart(
                                        items = reportsState.expensesByCategory,
                                        currency = preferences.currencySymbol
                                    )
                                }
                            }
                        }

                        // Bar Comparison Chart: Project Budgets vs Spent
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = strings.budgetVsSpentTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (reportsState.projectBudgetStats.isEmpty()) {
                                        Text(strings.noProjectsChartData, style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        ProjectBudgetBarsChart(
                                            stats = reportsState.projectBudgetStats,
                                            currency = preferences.currencySymbol
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Share Text Summary Button
                    item {
                        Button(
                            onClick = {
                                val summaryText = buildString {
                                    appendLine("${strings.companyNameLabel}: ${preferences.companyName}")
                                    appendLine("${strings.dateLabel}: ${Formatters.todayDate()}")
                                    appendLine("-----------------------")
                                    appendLine("${strings.tabProjects}: ${reportsState.totalProjects} (${strings.avgProjectProgress}: ${reportsState.averageProjectProgress}%)")
                                    appendLine("${strings.budgetLabel} ${Formatters.formatMoney(reportsState.totalBudget, preferences.currencySymbol)}")
                                    appendLine("${strings.totalExpenses}: ${Formatters.formatMoney(reportsState.totalExpenses, preferences.currencySymbol)}")
                                    appendLine("${strings.collectedRevenue}: ${Formatters.formatMoney(reportsState.totalCollected, preferences.currencySymbol)}")
                                    appendLine("${strings.presentWorkforce}: ${reportsState.presentWorkers}/${reportsState.totalWorkers}")
                                    appendLine("${strings.equipmentReadiness}: ${reportsState.activeEquipment}/${reportsState.totalEquipment}")
                                    appendLine("${strings.currentInventoryValue}: ${Formatters.formatMoney(reportsState.inventoryValue, preferences.currencySymbol)}")
                                }
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "BTP Manager - ${preferences.companyName}")
                                    putExtra(Intent.EXTRA_TEXT, summaryText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, strings.shareReportChooser))
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberSecondary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("export_report_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.exportShareReport, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Security, Users & Company Settings Tab
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (authState.statusMessage != null || authState.errorMessage != null) {
                        item {
                            val isError = authState.errorMessage != null
                            val rawMsg = authState.errorMessage ?: authState.statusMessage ?: ""
                            Surface(
                                color = if (isError) StatusDangerBg else StatusSuccessBg,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.authErrorText(rawMsg),
                                        color = if (isError) StatusDanger else StatusSuccess,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedButton(onClick = onClearMessages) { Text(strings.hide) }
                                }
                            }
                        }
                    }

                    // Current Session Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                            icon = Icons.Outlined.VerifiedUser,
                                            isAccentOrange = true,
                                            size = 42.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = authState.currentUser?.fullName ?: "-",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "@${authState.currentUser?.username ?: ""} • ${authState.currentUser?.role ?: ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    StatusChip(label = "نشط")
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showChangePasswordDialog = true },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(strings.changePasswordBtn, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Button(
                                        onClick = onLogout,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusDanger),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(strings.lockExitBtn, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }

                    // Company, Language & Theme Preferences
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BtpUnifiedModuleIcon(
                                        icon = Icons.Outlined.Business,
                                        isAccentOrange = false,
                                        size = 38.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(strings.companyThemeSettingsTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "${strings.companyCurrencySummaryFmt} ${preferences.companyName} • ${preferences.currencySymbol}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = { showCompanySettingsDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(strings.editCompanyCurrencyBtn)
                                }

                                OutlinedButton(
                                    onClick = { showLanguageDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("settings_language_button")
                                ) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = AmberSecondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("${strings.languageLabel}: ${currentLanguage.nativeName}")
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Brightness6, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.appearanceLabel, style = MaterialTheme.typography.labelLarge)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = preferences.darkThemeOverride == null,
                                        onClick = { onSetDarkThemeMode(null) },
                                        label = { Text(strings.themeAuto) },
                                        shape = RoundedCornerShape(50),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AmberSecondary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                    FilterChip(
                                        selected = preferences.darkThemeOverride == false,
                                        onClick = { onSetDarkThemeMode(false) },
                                        label = { Text(strings.themeLight) },
                                        shape = RoundedCornerShape(50),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AmberSecondary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                    FilterChip(
                                        selected = preferences.darkThemeOverride == true,
                                        onClick = { onSetDarkThemeMode(true) },
                                        label = { Text(strings.themeDark) },
                                        shape = RoundedCornerShape(50),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AmberSecondary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Users Management Header
                    item {
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
                                    icon = Icons.Outlined.AddModerator,
                                    isAccentOrange = false,
                                    size = 36.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(strings.userAccountsTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { showCreateUserDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary, contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.newAccountBtn, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (isWideTablet) {
                        val userRows = allUsers.chunked(2)
                        items(userRows) { rowUsers ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowUsers.forEach { user ->
                                    UserAccountRowCard(
                                        user = user,
                                        onToggleActive = { onToggleUserActive(user) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowUsers.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        items(allUsers, key = { it.id }) { user ->
                            UserAccountRowCard(
                                user = user,
                                onToggleActive = { onToggleUserActive(user) }
                            )
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, StatusDanger.copy(alpha = 0.55f))
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = StatusDanger)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.resetDemoDataBtn, color = StatusDanger, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            }
        }
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentLanguage = currentLanguage,
            onSelectLanguage = onSelectLanguage,
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showCreateUserDialog) {
        var fullName by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var role by remember { mutableStateOf(BtpConstants.userRoles.last()) }
        var password by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateUserDialog = false },
            title = { Text(strings.createUserTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BtpInputField(value = fullName, onValueChange = { fullName = it }, label = strings.fullNameLabel)
                    BtpInputField(value = phone, onValueChange = { phone = it }, label = strings.phoneLabel)
                    BtpInputField(value = username, onValueChange = { username = it }, label = strings.usernameLatinLabel)
                    BtpDropdownField(
                        label = strings.rolePermissionLabel,
                        selectedValue = role,
                        options = BtpConstants.userRoles,
                        onOptionSelected = { role = it }
                    )
                    BtpInputField(value = password, onValueChange = { password = it }, label = strings.passwordMin4Label)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateUser(fullName, username, phone, role, password)
                        showCreateUserDialog = false
                    }
                ) { Text(strings.createBtn) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreateUserDialog = false }) { Text(strings.cancel) }
            }
        )
    }

    if (showChangePasswordDialog) {
        var newPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text(strings.updatePasswordTitle) },
            text = {
                BtpInputField(value = newPass, onValueChange = { newPass = it }, label = strings.newPasswordLabel)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onChangePassword(newPass)
                        showChangePasswordDialog = false
                    }
                ) { Text(strings.updateBtn) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showChangePasswordDialog = false }) { Text(strings.cancel) }
            }
        )
    }

    if (showCompanySettingsDialog) {
        var compName by remember { mutableStateOf(preferences.companyName) }
        var curr by remember { mutableStateOf(preferences.currencySymbol) }
        AlertDialog(
            onDismissRequest = { showCompanySettingsDialog = false },
            title = { Text(strings.companySettingsTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BtpInputField(value = compName, onValueChange = { compName = it }, label = strings.companyNameLabel)
                    BtpDropdownField(
                        label = strings.defaultCurrencyLabel,
                        selectedValue = curr,
                        options = listOf("د.ج", "د.م", "د.ت", "ر.س", "د.إ", "€", "$"),
                        onOptionSelected = { curr = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateCompanySettings(compName, curr)
                        showCompanySettingsDialog = false
                    }
                ) { Text(strings.save) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCompanySettingsDialog = false }) { Text(strings.cancel) }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text(strings.resetDemoTitle) },
            text = { Text(strings.resetDemoMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        onResetSampleData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                ) { Text(strings.yesRestore) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) { Text(strings.cancel) }
            }
        )
    }
}

@Composable
private fun UserAccountRowCard(
    user: UserAccountEntity,
    onToggleActive: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                BtpUnifiedModuleIcon(
                    icon = Icons.Outlined.VerifiedUser,
                    isAccentOrange = user.isActive,
                    size = 38.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${user.phone} • @${user.username} • ${user.role}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Switch(
                checked = user.isActive,
                onCheckedChange = { onToggleActive() }
            )
        }
    }
}
