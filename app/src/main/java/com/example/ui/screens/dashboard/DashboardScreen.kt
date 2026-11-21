package com.example.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.util.Formatters
import com.example.data.local.entity.UserAccountEntity
import com.example.ui.components.BtpBrandEmblem
import com.example.ui.components.BtpUnifiedModuleIcon
import com.example.ui.components.LanguagePickerDialog
import com.example.ui.components.ProgressRing
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatKpiCard
import com.example.ui.components.StatusChip
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavyPrimaryDark
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.SteelTertiary
import com.example.viewmodel.DashboardAlertType
import com.example.viewmodel.DashboardUiState

enum class BtpDestinationTab {
    DASHBOARD, PROJECTS, RESOURCES, FINANCE, REPORTS
}

data class QuickModuleShortcut(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val isAlert: Boolean,
    val tag: String,
    val targetTab: BtpDestinationTab,
    val targetSubTab: Int
)

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    currentUser: UserAccountEntity?,
    companyName: String,
    currency: String,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onNavigate: (BtpDestinationTab, Int) -> Unit,
    onLogout: () -> Unit
) {
    val strings = LocalAppStrings.current
    var showLanguageDialog by remember { mutableStateOf(false) }

    // All 16 BTP enterprise modules styled cleanly with unified vivid category badges matching user mockup
    val modules = listOf(
        QuickModuleShortcut(
            title = strings.modProjects,
            subtitle = "${uiState.totalProjectsCount} ${strings.modProjectsSub}",
            icon = Icons.Filled.Apartment,
            accentColor = Color(0xFF2563EB),
            isAlert = false,
            tag = "shortcut_projects_0",
            targetTab = BtpDestinationTab.PROJECTS,
            targetSubTab = 0
        ),
        QuickModuleShortcut(
            title = strings.modEmployees,
            subtitle = "${uiState.presentEmployeesCount}/${uiState.totalEmployeesCount} ${strings.modEmployeesSub}",
            icon = Icons.Filled.Badge,
            accentColor = AmberSecondary,
            isAlert = false,
            tag = "shortcut_resources_0",
            targetTab = BtpDestinationTab.RESOURCES,
            targetSubTab = 0
        ),
        QuickModuleShortcut(
            title = strings.modEquipment,
            subtitle = "${uiState.activeEquipmentCount} ${strings.modEquipmentSub}",
            icon = Icons.Filled.PrecisionManufacturing,
            accentColor = Color(0xFFD97706),
            isAlert = false,
            tag = "shortcut_resources_1",
            targetTab = BtpDestinationTab.RESOURCES,
            targetSubTab = 1
        ),
        QuickModuleShortcut(
            title = strings.modSchedule,
            subtitle = "${uiState.upcomingTasks.size} ${strings.modScheduleSub}",
            icon = Icons.Filled.CalendarMonth,
            accentColor = Color(0xFF0284C7),
            isAlert = false,
            tag = "shortcut_projects_2",
            targetTab = BtpDestinationTab.PROJECTS,
            targetSubTab = 2
        ),
        QuickModuleShortcut(
            title = strings.modExpenses,
            subtitle = strings.modExpensesSub,
            icon = Icons.Filled.AccountBalanceWallet,
            accentColor = StatusSuccess,
            isAlert = false,
            tag = "shortcut_finance_0",
            targetTab = BtpDestinationTab.FINANCE,
            targetSubTab = 0
        ),
        QuickModuleShortcut(
            title = strings.suppliersLabel,
            subtitle = strings.modPartnersSub,
            icon = Icons.Filled.LocalShipping,
            accentColor = Color(0xFF4F46E5),
            isAlert = false,
            tag = "shortcut_finance_2",
            targetTab = BtpDestinationTab.FINANCE,
            targetSubTab = 2
        ),
        QuickModuleShortcut(
            title = strings.modInventory,
            subtitle = "${uiState.lowStockCount} ${strings.modInventorySub}",
            icon = Icons.Filled.Inventory2,
            accentColor = Color(0xFFB45309),
            isAlert = uiState.lowStockCount > 0,
            tag = "shortcut_resources_2",
            targetTab = BtpDestinationTab.RESOURCES,
            targetSubTab = 2
        ),
        QuickModuleShortcut(
            title = strings.modReports,
            subtitle = strings.modReportsSub,
            icon = Icons.Filled.Assessment,
            accentColor = AmberSecondary,
            isAlert = false,
            tag = "shortcut_reports_0",
            targetTab = BtpDestinationTab.REPORTS,
            targetSubTab = 0
        ),
        QuickModuleShortcut(
            title = strings.modSites,
            subtitle = "${uiState.activeSitesCount} ${strings.modSitesSub}",
            icon = Icons.Filled.Domain,
            accentColor = NavyPrimary,
            isAlert = false,
            tag = "shortcut_projects_1",
            targetTab = BtpDestinationTab.PROJECTS,
            targetSubTab = 1
        ),
        QuickModuleShortcut(
            title = strings.clientsLabel,
            subtitle = strings.modPartnersSub,
            icon = Icons.Filled.Handshake,
            accentColor = Color(0xFF7C3AED),
            isAlert = false,
            tag = "shortcut_clients",
            targetTab = BtpDestinationTab.FINANCE,
            targetSubTab = 2
        ),
        QuickModuleShortcut(
            title = strings.modInvoices,
            subtitle = "${uiState.overdueInvoicesCount} ${strings.modInvoicesSub}",
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            accentColor = SteelTertiary,
            isAlert = uiState.overdueInvoicesCount > 0,
            tag = "shortcut_finance_1",
            targetTab = BtpDestinationTab.FINANCE,
            targetSubTab = 1
        ),
        QuickModuleShortcut(
            title = strings.recordPaymentTitle,
            subtitle = Formatters.formatMoney(uiState.totalCollectedRevenue, currency),
            icon = Icons.Filled.Payments,
            accentColor = Color(0xFF0D9488),
            isAlert = false,
            tag = "shortcut_payments",
            targetTab = BtpDestinationTab.FINANCE,
            targetSubTab = 1
        ),
        QuickModuleShortcut(
            title = strings.modSecurity,
            subtitle = strings.modSecuritySub,
            icon = Icons.Filled.Security,
            accentColor = Color(0xFF1D4ED8),
            isAlert = false,
            tag = "shortcut_reports_1",
            targetTab = BtpDestinationTab.REPORTS,
            targetSubTab = 1
        ),
        QuickModuleShortcut(
            title = strings.attendanceStatusLabel,
            subtitle = "${uiState.presentEmployeesCount} ${strings.translateTerm("حاضر")}",
            icon = Icons.Filled.FactCheck,
            accentColor = StatusSuccess,
            isAlert = false,
            tag = "shortcut_attendance",
            targetTab = BtpDestinationTab.RESOURCES,
            targetSubTab = 0
        ),
        QuickModuleShortcut(
            title = strings.expensesByCategoryTitle,
            subtitle = strings.tabReports,
            icon = Icons.Filled.BarChart,
            accentColor = Color(0xFFEA580C),
            isAlert = false,
            tag = "shortcut_charts",
            targetTab = BtpDestinationTab.REPORTS,
            targetSubTab = 0
        ),
        QuickModuleShortcut(
            title = strings.companySettingsTitle,
            subtitle = currency,
            icon = Icons.Filled.Business,
            accentColor = Color(0xFF475569),
            isAlert = false,
            tag = "shortcut_company_settings",
            targetTab = BtpDestinationTab.REPORTS,
            targetSubTab = 1
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Compact Contextual Welcome Strip beneath Persistent Top Navigation Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${strings.welcomePrefix} ${currentUser?.fullName ?: ""} • $companyName",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (uiState.alerts.isNotEmpty()) {
                    Surface(
                        color = StatusDanger.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                    ) {
                        Text(
                            text = "${uiState.alerts.size} ${strings.activeAlerts}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusDanger,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val isTablet = maxWidth >= 680.dp
            val moduleColumns = if (isTablet) 4 else 2

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Full-Width Modern Construction Site Hero Banner Card (Matches Right Phone Banner)
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyPrimaryDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isTablet) 210.dp else 188.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_btp_site_hero),
                                contentDescription = "BTP Manager Hero",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                NavyPrimaryDark.copy(alpha = 0.45f),
                                                NavyPrimaryDark.copy(alpha = 0.94f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = companyName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = AmberBright,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${strings.netOperatingBalance}: ${Formatters.formatMoney(uiState.netBalance, currency)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HeroMetricPill(
                                        label = strings.collectedRevenue,
                                        value = Formatters.formatMoney(uiState.totalCollectedRevenue, currency),
                                        valueColor = Color(0xFF4ADE80),
                                        modifier = Modifier.weight(1f)
                                    )
                                    HeroMetricPill(
                                        label = strings.totalExpenses,
                                        value = Formatters.formatMoney(uiState.totalExpenses, currency),
                                        valueColor = Color(0xFFFCA5A5),
                                        modifier = Modifier.weight(1f)
                                    )
                                    HeroMetricPill(
                                        label = strings.pendingReceivables,
                                        value = Formatters.formatMoney(uiState.pendingReceivables, currency),
                                        valueColor = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2x2 Compact White KPI Cards (1x4 on Tablet — Matches Mockup Right Phone)
                item {
                    if (isTablet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatKpiCard(
                                title = strings.activeProjects,
                                value = "${uiState.activeProjectsCount}",
                                subtitle = "${strings.ofTotalProjects} ${uiState.totalProjectsCount}",
                                icon = Icons.Filled.Apartment,
                                accentColor = Color(0xFF2563EB),
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate(BtpDestinationTab.PROJECTS, 0) }
                            )
                            StatKpiCard(
                                title = strings.sitesAndWorkshops,
                                value = "${uiState.activeSitesCount}",
                                subtitle = strings.activeSitesSub,
                                icon = Icons.Filled.CheckCircle,
                                accentColor = StatusSuccess,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate(BtpDestinationTab.PROJECTS, 1) }
                            )
                            StatKpiCard(
                                title = strings.presentWorkforce,
                                value = "${uiState.presentEmployeesCount}/${uiState.totalEmployeesCount}",
                                subtitle = "${strings.activeEquipmentLabel} ${uiState.activeEquipmentCount}",
                                icon = Icons.Filled.Groups,
                                accentColor = AmberSecondary,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate(BtpDestinationTab.RESOURCES, 0) }
                            )
                            StatKpiCard(
                                title = strings.activeAlerts,
                                value = "${uiState.alerts.size}",
                                subtitle = "${strings.stockAndInvoicesSub}: ${uiState.lowStockCount}/${uiState.overdueInvoicesCount}",
                                icon = Icons.Filled.PrecisionManufacturing,
                                accentColor = if (uiState.alerts.isNotEmpty()) StatusDanger else Color(0xFF7C3AED),
                                isAlert = uiState.alerts.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate(BtpDestinationTab.RESOURCES, 2) }
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatKpiCard(
                                    title = strings.activeProjects,
                                    value = "${uiState.activeProjectsCount}",
                                    subtitle = "${strings.ofTotalProjects} ${uiState.totalProjectsCount}",
                                    icon = Icons.Filled.Apartment,
                                    accentColor = Color(0xFF2563EB),
                                    modifier = Modifier.weight(1f),
                                    onClick = { onNavigate(BtpDestinationTab.PROJECTS, 0) }
                                )
                                StatKpiCard(
                                    title = strings.sitesAndWorkshops,
                                    value = "${uiState.activeSitesCount}",
                                    subtitle = strings.activeSitesSub,
                                    icon = Icons.Filled.CheckCircle,
                                    accentColor = StatusSuccess,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onNavigate(BtpDestinationTab.PROJECTS, 1) }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatKpiCard(
                                    title = strings.presentWorkforce,
                                    value = "${uiState.presentEmployeesCount}/${uiState.totalEmployeesCount}",
                                    subtitle = "${strings.activeEquipmentLabel} ${uiState.activeEquipmentCount}",
                                    icon = Icons.Filled.Groups,
                                    accentColor = AmberSecondary,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onNavigate(BtpDestinationTab.RESOURCES, 0) }
                                )
                                StatKpiCard(
                                    title = strings.activeAlerts,
                                    value = "${uiState.alerts.size}",
                                    subtitle = "${strings.stockAndInvoicesSub}: ${uiState.lowStockCount}/${uiState.overdueInvoicesCount}",
                                    icon = Icons.Filled.PrecisionManufacturing,
                                    accentColor = if (uiState.alerts.isNotEmpty()) StatusDanger else Color(0xFF7C3AED),
                                    isAlert = uiState.alerts.isNotEmpty(),
                                    modifier = Modifier.weight(1f),
                                    onClick = { onNavigate(BtpDestinationTab.RESOURCES, 2) }
                                )
                            }
                        }
                    }
                }

                // All 16 BTP Enterprise Modules (Matches the Bottom 16 Horizontal White Cards Showcase in Mockup)
                item {
                    SectionHeader(
                        title = strings.systemModulesTitle,
                        actionLabel = strings.viewAll,
                        onActionClick = { onNavigate(BtpDestinationTab.PROJECTS, 0) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        modules.chunked(moduleColumns).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { shortcut ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onNavigate(shortcut.targetTab, shortcut.targetSubTab) }
                                            .testTag(shortcut.tag),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(
                                            1.dp,
                                            if (shortcut.isAlert) StatusDanger.copy(alpha = 0.45f)
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            BtpUnifiedModuleIcon(
                                                icon = shortcut.icon,
                                                isAlert = shortcut.isAlert,
                                                accentColor = shortcut.accentColor,
                                                size = 40.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = shortcut.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = shortcut.subtitle,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                                repeat(moduleColumns - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Active Alerts Strip
                if (uiState.alerts.isNotEmpty()) {
                    item {
                        SectionHeader(title = strings.instantAlertsTitle)
                    }
                    items(uiState.alerts.take(3)) { alert ->
                        val (alertTitle, alertSubtitle) = when (alert.type) {
                            DashboardAlertType.LOW_STOCK ->
                                "${strings.lowStockAlertPrefix} ${alert.subject}" to
                                    "${strings.availableQtyFmt}: ${alert.detailPrimary} (${alert.detailSecondary})"
                            DashboardAlertType.OVERDUE_INVOICE ->
                                "${strings.overdueInvoicePrefix} ${alert.subject}" to
                                    "${alert.detailPrimary} • ${strings.duePrefix} ${alert.detailSecondary}"
                            DashboardAlertType.MAINTENANCE_EQUIPMENT ->
                                "${strings.maintenanceEqPrefix} ${alert.subject}" to
                                    "${alert.detailPrimary} • ${strings.nextDatePrefix} ${alert.detailSecondary}"
                            DashboardAlertType.SAFETY_ALERT ->
                                "${strings.safetyAlertPrefix} ${alert.subject}" to
                                    alert.detailPrimary.ifBlank { strings.safetyCheckFallback }
                        }
                        Surface(
                            color = if (alert.isUrgent) StatusDangerBg else StatusWarningBg,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                (if (alert.isUrgent) StatusDanger else StatusWarning).copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BtpUnifiedModuleIcon(
                                    icon = Icons.Default.WarningAmber,
                                    isAlert = alert.isUrgent,
                                    accentColor = if (alert.isUrgent) StatusDanger else StatusWarning,
                                    size = 36.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alertTitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (alert.isUrgent) StatusDanger else StatusWarning,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = alertSubtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Active Projects Preview
                item {
                    SectionHeader(
                        title = strings.ongoingProjectsTitle,
                        actionLabel = strings.viewAll,
                        onActionClick = { onNavigate(BtpDestinationTab.PROJECTS, 0) }
                    )
                }
                items(uiState.recentProjects) { project ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(BtpDestinationTab.PROJECTS, 0) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProgressRing(
                                progressPercent = project.progressPercent,
                                color = if (project.progressPercent >= 75) StatusSuccess else AmberSecondary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = project.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    StatusChip(label = project.status)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${project.clientName} • ${project.location}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (project.progressPercent / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = AmberSecondary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
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
}

@Composable
private fun HeroMetricPill(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NavyPrimaryDark.copy(alpha = 0.65f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.22f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.80f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = valueColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
