package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.BtpApplication
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.ui.components.BtpBrandEmblem
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.dashboard.BtpDestinationTab
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.finance.FinanceHubScreen
import com.example.ui.screens.projects.ProjectsHubScreen
import com.example.ui.screens.reports.ReportsAndSecurityScreen
import com.example.ui.screens.resources.ResourcesHubScreen
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.BtpManagerTheme
import com.example.ui.theme.NavyPrimaryDark
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.BtpViewModelFactory
import com.example.viewmodel.DashboardViewModel
import com.example.viewmodel.FinanceViewModel
import com.example.viewmodel.ProjectsViewModel
import com.example.viewmodel.ReportsViewModel
import com.example.viewmodel.ResourcesViewModel

private data class NavItem(
    val tab: BtpDestinationTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun BtpAppRoot(application: BtpApplication) {
    val factory = remember(application) { BtpViewModelFactory(application) }
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val preferences by authViewModel.preferences.collectAsStateWithLifecycle()
    val allUsers by authViewModel.allUsers.collectAsStateWithLifecycle()

    val isDark = preferences.darkThemeOverride ?: isSystemInDarkTheme()
    val currentLanguage = AppLanguage.fromCode(preferences.languageCode)

    BtpManagerTheme(darkTheme = isDark, language = currentLanguage) {
        val strings = LocalAppStrings.current
        val mainNavItems = remember(strings) {
            listOf(
                NavItem(BtpDestinationTab.DASHBOARD, strings.navDashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
                NavItem(BtpDestinationTab.PROJECTS, strings.navProjects, Icons.Filled.Apartment, Icons.Outlined.Apartment),
                NavItem(BtpDestinationTab.RESOURCES, strings.navResources, Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
                NavItem(BtpDestinationTab.FINANCE, strings.navFinance, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
                NavItem(BtpDestinationTab.REPORTS, strings.navReports, Icons.Filled.Assessment, Icons.Outlined.Assessment)
            )
        }

        when {
            authState.isInitializing -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BtpBrandEmblem(size = 76.dp, onDarkBackground = true)
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = AmberSecondary)
                    }
                }
            }
            authState.currentUser == null -> {
                LoginScreen(
                    uiState = authState,
                    companyName = preferences.companyName,
                    currentLanguage = currentLanguage,
                    onSelectLanguage = authViewModel::setLanguage,
                    onLogin = authViewModel::login,
                    onLoginDemo = authViewModel::loginDemo,
                    onClearError = authViewModel::clearMessages
                )
            }
            else -> {
                val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
                val projectsViewModel: ProjectsViewModel = viewModel(factory = factory)
                val resourcesViewModel: ResourcesViewModel = viewModel(factory = factory)
                val financeViewModel: FinanceViewModel = viewModel(factory = factory)
                val reportsViewModel: ReportsViewModel = viewModel(factory = factory)

                val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                val projectsState by projectsViewModel.uiState.collectAsStateWithLifecycle()
                val resourcesState by resourcesViewModel.uiState.collectAsStateWithLifecycle()
                val financeState by financeViewModel.uiState.collectAsStateWithLifecycle()
                val reportsState by reportsViewModel.uiState.collectAsStateWithLifecycle()

                var currentTab by rememberSaveable { mutableStateOf(BtpDestinationTab.DASHBOARD) }
                var projectsSubTab by rememberSaveable { mutableIntStateOf(0) }
                var resourcesSubTab by rememberSaveable { mutableIntStateOf(0) }
                var financeSubTab by rememberSaveable { mutableIntStateOf(0) }
                var reportsSubTab by rememberSaveable { mutableIntStateOf(0) }

                LaunchedEffect(authState.currentUser?.id) {
                    currentTab = BtpDestinationTab.DASHBOARD
                }

                BackHandler(enabled = currentTab != BtpDestinationTab.DASHBOARD) {
                    currentTab = BtpDestinationTab.DASHBOARD
                }

                val navigateTo: (BtpDestinationTab, Int) -> Unit = { tab, subTab ->
                    when (tab) {
                        BtpDestinationTab.PROJECTS -> projectsSubTab = subTab
                        BtpDestinationTab.RESOURCES -> resourcesSubTab = subTab
                        BtpDestinationTab.FINANCE -> financeSubTab = subTab
                        BtpDestinationTab.REPORTS -> reportsSubTab = subTab
                        BtpDestinationTab.DASHBOARD -> Unit
                    }
                    currentTab = tab
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useRail = maxWidth >= 700.dp

                    Scaffold(
                        contentWindowInsets = WindowInsets.navigationBars,
                        bottomBar = {
                            if (!useRail) {
                                NavigationBar(
                                    containerColor = NavyPrimaryDark,
                                    tonalElevation = 0.dp
                                ) {
                                    mainNavItems.forEach { item ->
                                        val selected = currentTab == item.tab
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { currentTab = item.tab },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.label
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = item.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = AmberSecondary,
                                                selectedTextColor = AmberSecondary,
                                                indicatorColor = Color.White.copy(alpha = 0.12f),
                                                unselectedIconColor = Color.White.copy(alpha = 0.78f),
                                                unselectedTextColor = Color.White.copy(alpha = 0.78f)
                                            ),
                                            modifier = Modifier.testTag("nav_${item.tab.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (useRail) {
                                NavigationRail(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .statusBarsPadding(),
                                    containerColor = NavyPrimaryDark,
                                    header = {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(top = 12.dp, bottom = 10.dp)
                                        ) {
                                            BtpBrandEmblem(size = 44.dp, onDarkBackground = true)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Surface(
                                                color = AmberSecondary,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "BTP",
                                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    mainNavItems.forEach { item ->
                                        val selected = currentTab == item.tab
                                        NavigationRailItem(
                                            selected = selected,
                                            onClick = { currentTab = item.tab },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.label
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = item.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            colors = NavigationRailItemDefaults.colors(
                                                selectedIconColor = AmberSecondary,
                                                selectedTextColor = AmberSecondary,
                                                indicatorColor = Color.White.copy(alpha = 0.14f),
                                                unselectedIconColor = Color.White.copy(alpha = 0.78f),
                                                unselectedTextColor = Color.White.copy(alpha = 0.78f)
                                            )
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .widthIn(max = 1120.dp)
                                ) {
                                    Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                                        when (tab) {
                                            BtpDestinationTab.DASHBOARD -> DashboardScreen(
                                                uiState = dashboardState,
                                                currentUser = authState.currentUser,
                                                companyName = preferences.companyName,
                                                currency = preferences.currencySymbol,
                                                currentLanguage = currentLanguage,
                                                onSelectLanguage = authViewModel::setLanguage,
                                                onNavigate = navigateTo,
                                                onLogout = authViewModel::logout
                                            )
                                            BtpDestinationTab.PROJECTS -> ProjectsHubScreen(
                                                uiState = projectsState,
                                                selectedSubTab = projectsSubTab,
                                                onSubTabChange = { projectsSubTab = it },
                                                currency = preferences.currencySymbol,
                                                onSearchChange = projectsViewModel::setSearchQuery,
                                                onStatusFilterChange = projectsViewModel::setStatusFilter,
                                                onSaveProject = projectsViewModel::saveProject,
                                                onDeleteProject = projectsViewModel::deleteProject,
                                                onSaveSite = projectsViewModel::saveSite,
                                                onDeleteSite = projectsViewModel::deleteSite,
                                                onSaveTask = projectsViewModel::saveTask,
                                                onToggleTaskComplete = projectsViewModel::toggleTaskComplete,
                                                onDeleteTask = projectsViewModel::deleteTask
                                            )
                                            BtpDestinationTab.RESOURCES -> ResourcesHubScreen(
                                                uiState = resourcesState,
                                                selectedSubTab = resourcesSubTab,
                                                onSubTabChange = { resourcesSubTab = it },
                                                currency = preferences.currencySymbol,
                                                onSearchChange = resourcesViewModel::setSearchQuery,
                                                onToggleLowStockOnly = resourcesViewModel::toggleLowStockOnly,
                                                onCycleEmployeeAttendance = resourcesViewModel::cycleEmployeeAttendance,
                                                onSaveEmployee = resourcesViewModel::saveEmployee,
                                                onDeleteEmployee = resourcesViewModel::deleteEmployee,
                                                onCycleEquipmentStatus = resourcesViewModel::cycleEquipmentStatus,
                                                onSaveEquipment = resourcesViewModel::saveEquipment,
                                                onDeleteEquipment = resourcesViewModel::deleteEquipment,
                                                onAdjustMaterialStock = resourcesViewModel::adjustMaterialStock,
                                                onSaveMaterial = resourcesViewModel::saveMaterial,
                                                onDeleteMaterial = resourcesViewModel::deleteMaterial
                                            )
                                            BtpDestinationTab.FINANCE -> FinanceHubScreen(
                                                uiState = financeState,
                                                projectNames = projectsState.projects.map { it.name },
                                                selectedSubTab = financeSubTab,
                                                onSubTabChange = { financeSubTab = it },
                                                currency = preferences.currencySymbol,
                                                onSearchChange = financeViewModel::setSearchQuery,
                                                onExpenseCategoryChange = financeViewModel::setExpenseCategoryFilter,
                                                onSaveExpense = financeViewModel::saveExpense,
                                                onDeleteExpense = financeViewModel::deleteExpense,
                                                onSaveInvoice = financeViewModel::saveInvoice,
                                                onRecordPayment = financeViewModel::recordPayment,
                                                onDeleteInvoice = financeViewModel::deleteInvoice,
                                                onSaveClient = financeViewModel::saveClient,
                                                onDeleteClient = financeViewModel::deleteClient,
                                                onSaveSupplier = financeViewModel::saveSupplier,
                                                onDeleteSupplier = financeViewModel::deleteSupplier
                                            )
                                            BtpDestinationTab.REPORTS -> ReportsAndSecurityScreen(
                                                reportsState = reportsState,
                                                authState = authState,
                                                preferences = preferences,
                                                allUsers = allUsers,
                                                currentLanguage = currentLanguage,
                                                onSelectLanguage = authViewModel::setLanguage,
                                                selectedSubTab = reportsSubTab,
                                                onSubTabChange = { reportsSubTab = it },
                                                onCreateUser = authViewModel::createUser,
                                                onChangePassword = authViewModel::changeCurrentPassword,
                                                onToggleUserActive = authViewModel::toggleUserActive,
                                                onSetDarkThemeMode = authViewModel::setDarkThemeMode,
                                                onUpdateCompanySettings = authViewModel::updateCompanySettings,
                                                onResetSampleData = authViewModel::resetDemoData,
                                                onLogout = authViewModel::logout,
                                                onClearMessages = authViewModel::clearMessages
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
    }
}
