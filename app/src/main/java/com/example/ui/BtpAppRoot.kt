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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.components.BtpTopNavigationBar
import com.example.ui.components.LanguagePickerDialog
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.dashboard.BtpDestinationTab
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.finance.ExpenseFormDialog
import com.example.ui.screens.finance.FinanceHubScreen
import com.example.ui.screens.projects.ProjectFormDialog
import com.example.ui.screens.projects.ProjectsHubScreen
import com.example.ui.screens.projects.ScheduleTaskFormDialog
import com.example.ui.screens.reports.ReportsAndSecurityScreen
import com.example.ui.screens.resources.EmployeeFormDialog
import com.example.ui.screens.resources.EquipmentFormDialog
import com.example.ui.screens.resources.ResourcesHubScreen
import com.example.ui.screens.topbar.BtpChatDialog
import com.example.ui.screens.topbar.FaultReportDialog
import com.example.ui.screens.topbar.GlobalSearchDialog
import com.example.ui.screens.topbar.NotificationsCenterDialog
import com.example.ui.screens.topbar.QuickActionType
import com.example.ui.screens.topbar.QuickActionsDialog
import com.example.ui.screens.topbar.SyncStatusDialog
import com.example.ui.screens.topbar.TasksCenterDialog
import com.example.ui.screens.topbar.UserProfileDialog
import com.example.ui.screens.topbar.notificationTargetTab
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
import com.example.viewmodel.TopBarCenterViewModel
import kotlinx.coroutines.launch

private data class NavItem(
    val tab: BtpDestinationTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private enum class ActiveTopBarSheet {
    NONE,
    CHAT,
    NOTIFICATIONS,
    PROFILE,
    SEARCH,
    QUICK_ACTIONS,
    TASKS,
    SYNC,
    LANGUAGE
}

@Composable
fun BtpAppRoot(application: BtpApplication) {
    val factory = remember(application) { BtpViewModelFactory(application) }
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val preferences by authViewModel.preferences.collectAsStateWithLifecycle()
    val allUsers by authViewModel.allUsers.collectAsStateWithLifecycle()

    val isDark = preferences.darkThemeOverride ?: isSystemInDarkTheme()
    val activeLangCode = authState.currentUser?.preferredLanguageCode ?: preferences.languageCode
    val currentLanguage = AppLanguage.fromCode(activeLangCode)

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
                val topBarViewModel: TopBarCenterViewModel = viewModel(factory = factory)

                val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                val projectsState by projectsViewModel.uiState.collectAsStateWithLifecycle()
                val resourcesState by resourcesViewModel.uiState.collectAsStateWithLifecycle()
                val financeState by financeViewModel.uiState.collectAsStateWithLifecycle()
                val reportsState by reportsViewModel.uiState.collectAsStateWithLifecycle()

                // Top Bar reactive states
                val permissions by topBarViewModel.permissions.collectAsStateWithLifecycle()
                val filteredChannels by topBarViewModel.filteredChannels.collectAsStateWithLifecycle()
                val selectedChannelId by topBarViewModel.selectedChannelId.collectAsStateWithLifecycle()
                val selectedChannelMessages by topBarViewModel.selectedChannelMessages.collectAsStateWithLifecycle()
                val chatFilterType by topBarViewModel.chatFilterType.collectAsStateWithLifecycle()
                val chatSearchQuery by topBarViewModel.chatSearchQuery.collectAsStateWithLifecycle()
                val unreadChatCount by topBarViewModel.unreadChatCount.collectAsStateWithLifecycle()

                val visibleNotifications by topBarViewModel.visibleNotifications.collectAsStateWithLifecycle()
                val notificationPreferences by topBarViewModel.notificationPreferences.collectAsStateWithLifecycle()
                val unreadNotificationsCount by topBarViewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

                val activeDevices by topBarViewModel.activeUserDevices.collectAsStateWithLifecycle()

                val globalSearchQuery by topBarViewModel.globalSearchQuery.collectAsStateWithLifecycle()
                val globalSearchCategory by topBarViewModel.globalSearchCategory.collectAsStateWithLifecycle()
                val globalSearchResults by topBarViewModel.globalSearchResults.collectAsStateWithLifecycle()

                val centerTasks by topBarViewModel.tasks.collectAsStateWithLifecycle()
                val pendingTasksCount by topBarViewModel.pendingOrOverdueTasksCount.collectAsStateWithLifecycle()
                val projectsList by topBarViewModel.projectsList.collectAsStateWithLifecycle()
                val sitesList by topBarViewModel.sitesList.collectAsStateWithLifecycle()
                val equipmentList by topBarViewModel.equipmentList.collectAsStateWithLifecycle()

                val syncSnapshot by topBarViewModel.syncSnapshot.collectAsStateWithLifecycle()
                val syncOutbox by topBarViewModel.syncOutbox.collectAsStateWithLifecycle()

                var currentTab by rememberSaveable { mutableStateOf(BtpDestinationTab.DASHBOARD) }
                var projectsSubTab by rememberSaveable { mutableIntStateOf(0) }
                var resourcesSubTab by rememberSaveable { mutableIntStateOf(0) }
                var financeSubTab by rememberSaveable { mutableIntStateOf(0) }
                var reportsSubTab by rememberSaveable { mutableIntStateOf(0) }

                var activeSheet by rememberSaveable { mutableStateOf(ActiveTopBarSheet.NONE) }
                var showQuickWorkerDialog by remember { mutableStateOf(false) }
                var showQuickProjectDialog by remember { mutableStateOf(false) }
                var showQuickExpenseDialog by remember { mutableStateOf(false) }
                var showQuickEquipmentDialog by remember { mutableStateOf(false) }
                var showQuickFaultDialog by remember { mutableStateOf(false) }
                var showQuickTaskDialog by remember { mutableStateOf(false) }

                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(authState.currentUser) {
                    topBarViewModel.setActiveUser(authState.currentUser)
                }

                LaunchedEffect(authState.currentUser?.id) {
                    currentTab = BtpDestinationTab.DASHBOARD
                    activeSheet = ActiveTopBarSheet.NONE
                }

                BackHandler(enabled = activeSheet != ActiveTopBarSheet.NONE || currentTab != BtpDestinationTab.DASHBOARD) {
                    if (activeSheet != ActiveTopBarSheet.NONE) {
                        activeSheet = ActiveTopBarSheet.NONE
                    } else {
                        currentTab = BtpDestinationTab.DASHBOARD
                    }
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

                val sectionTitle = when (currentTab) {
                    BtpDestinationTab.DASHBOARD -> strings.navDashboard
                    BtpDestinationTab.PROJECTS -> strings.navProjects
                    BtpDestinationTab.RESOURCES -> strings.navResources
                    BtpDestinationTab.FINANCE -> strings.navFinance
                    BtpDestinationTab.REPORTS -> strings.navReports
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useRail = maxWidth >= 700.dp

                    Scaffold(
                        contentWindowInsets = WindowInsets.navigationBars,
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        topBar = {
                            BtpTopNavigationBar(
                                currentUser = authState.currentUser,
                                companyName = preferences.companyName,
                                sectionTitle = sectionTitle,
                                currentLanguage = currentLanguage,
                                unreadChatCount = unreadChatCount,
                                unreadNotificationsCount = unreadNotificationsCount,
                                pendingTasksCount = pendingTasksCount,
                                syncSnapshot = syncSnapshot,
                                onOpenChat = { activeSheet = ActiveTopBarSheet.CHAT },
                                onOpenNotifications = { activeSheet = ActiveTopBarSheet.NOTIFICATIONS },
                                onOpenProfile = { activeSheet = ActiveTopBarSheet.PROFILE },
                                onOpenGlobalSearch = { activeSheet = ActiveTopBarSheet.SEARCH },
                                onOpenQuickActions = { activeSheet = ActiveTopBarSheet.QUICK_ACTIONS },
                                onOpenTasksCenter = { activeSheet = ActiveTopBarSheet.TASKS },
                                onOpenSyncStatus = { activeSheet = ActiveTopBarSheet.SYNC },
                                onOpenLanguagePicker = { activeSheet = ActiveTopBarSheet.LANGUAGE },
                                onLogout = authViewModel::logout
                            )
                        },
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
                                    modifier = Modifier.fillMaxHeight(),
                                    containerColor = NavyPrimaryDark,
                                    header = {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(top = 12.dp, bottom = 10.dp)
                                        ) {
                                            Surface(
                                                color = AmberSecondary,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "BTP",
                                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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

                    // Active Top Navigation Bar Overlay Centers
                    when (activeSheet) {
                        ActiveTopBarSheet.CHAT -> {
                            BtpChatDialog(
                                currentUser = authState.currentUser,
                                permissions = permissions,
                                channels = filteredChannels,
                                selectedChannelId = selectedChannelId,
                                messages = selectedChannelMessages,
                                filterType = chatFilterType,
                                searchQuery = chatSearchQuery,
                                allUsers = allUsers,
                                projects = projectsList,
                                onSelectChannel = topBarViewModel::selectChannel,
                                onFilterTypeChange = topBarViewModel::setChatFilterType,
                                onSearchQueryChange = topBarViewModel::setChatSearchQuery,
                                onSendMessage = topBarViewModel::sendChatMessage,
                                onStartDirectChat = topBarViewModel::startDirectConversation,
                                onCreateGroupChannel = topBarViewModel::createGroupChannel,
                                onMarkAllRead = topBarViewModel::markAllChannelsRead,
                                onDismiss = { activeSheet = ActiveTopBarSheet.NONE }
                            )
                        }
                        ActiveTopBarSheet.NOTIFICATIONS -> {
                            NotificationsCenterDialog(
                                notifications = visibleNotifications,
                                preferences = notificationPreferences,
                                permissions = permissions,
                                onMarkRead = topBarViewModel::markNotificationRead,
                                onMarkAllRead = topBarViewModel::markAllNotificationsRead,
                                onTogglePreference = topBarViewModel::toggleNotificationPreference,
                                onNavigateFromNotification = { notif ->
                                    if (notif.category == "CHAT") {
                                        activeSheet = ActiveTopBarSheet.CHAT
                                    } else {
                                        activeSheet = ActiveTopBarSheet.NONE
                                        navigateTo(notificationTargetTab(notif.targetTab), notif.targetSubTab)
                                    }
                                },
                                onDismiss = { activeSheet = ActiveTopBarSheet.NONE }
                            )
                        }
                        ActiveTopBarSheet.PROFILE -> {
                            val currentUser = authState.currentUser
                            if (currentUser != null) {
                                UserProfileDialog(
                                    user = currentUser,
                                    companyName = preferences.companyName,
                                    currentLanguage = currentLanguage,
                                    devices = activeDevices,
                                    onUpdateProfile = authViewModel::updateCurrentUserProfile,
                                    onVerifyAndChangePassword = authViewModel::verifyAndChangePassword,
                                    onOpenLanguagePicker = { activeSheet = ActiveTopBarSheet.LANGUAGE },
                                    onRevokeDevice = topBarViewModel::revokeDeviceSession,
                                    onLogout = authViewModel::logout,
                                    onDismiss = { activeSheet = ActiveTopBarSheet.NONE }
                                )
                            }
                        }
                        ActiveTopBarSheet.SEARCH -> {
                            GlobalSearchDialog(
                                query = globalSearchQuery,
                                selectedCategory = globalSearchCategory,
                                results = globalSearchResults,
                                permissions = permissions,
                                onQueryChange = topBarViewModel::setGlobalSearchQuery,
                                onCategoryChange = topBarViewModel::setGlobalSearchCategory,
                                onResultClick = { result ->
                                    if (result.openChatChannelId != null) {
                                        topBarViewModel.selectChannel(result.openChatChannelId)
                                        activeSheet = ActiveTopBarSheet.CHAT
                                    } else {
                                        activeSheet = ActiveTopBarSheet.NONE
                                        navigateTo(result.targetTab, result.targetSubTab)
                                    }
                                },
                                onDismiss = { activeSheet = ActiveTopBarSheet.NONE }
                            )
                        }
                        ActiveTopBarSheet.QUICK_ACTIONS -> {
                            QuickActionsDialog(
                                permissions = permissions,
                                onTriggerAction = { action ->
                                    activeSheet = ActiveTopBarSheet.NONE
                                    when (action) {
                                        QuickActionType.ADD_WORKER -> showQuickWorkerDialog = true
                                        QuickActionType.ADD_PROJECT -> showQuickProjectDialog = true
                                        QuickActionType.RECORD_EXPENSE -> showQuickExpenseDialog = true
                                        QuickActionType.ADD_EQUIPMENT -> showQuickEquipmentDialog = true
                                        QuickActionType.REPORT_FAULT -> showQuickFaultDialog = true
                                        QuickActionType.SEND_MESSAGE -> activeSheet = ActiveTopBarSheet.CHAT
                                        QuickActionType.CREATE_TASK -> showQuickTaskDialog = true
                                    }
                                },
                                onDismiss = { activeSheet = ActiveTopBarSheet.NONE }
                            )
                        }
                        ActiveTopBarSheet.TASKS -> {
                            TasksCenterDialog(
                                currentUser = authState.currentUser,
                                tasks = centerTasks,
                                onToggleComplete = topBarViewModel::toggleTaskComplete,
                                onUpdateProgress = topBarViewModel::updateTaskProgress,
                                onAddTaskClick = {
                                    activeSheet = ActiveTopBarSheet.NONE
                                    showQuickTaskDialog = true
                                },
                                onDismiss = { activeSheet = ActiveTopBarSheet.NONE }
                            )
                        }
                        ActiveTopBarSheet.SYNC -> {
                            SyncStatusDialog(
                                snapshot = syncSnapshot,
                                outbox = syncOutbox,
                                onSyncNow = topBarViewModel::syncNow,
                                onRetryFailed = topBarViewModel::retryFailedSync,
                                onClearSynced = topBarViewModel::clearSyncedOutbox,
                                onDismiss = { activeSheet = ActiveTopBarSheet.NONE }
                            )
                        }
                        ActiveTopBarSheet.LANGUAGE -> {
                            LanguagePickerDialog(
                                currentLanguage = currentLanguage,
                                onSelectLanguage = authViewModel::setLanguage,
                                onDismiss = { activeSheet = ActiveTopBarSheet.NONE }
                            )
                        }
                        ActiveTopBarSheet.NONE -> Unit
                    }

                    // Quick Actions target dialogs
                    if (showQuickWorkerDialog) {
                        EmployeeFormDialog(
                            initial = null,
                            onSave = {
                                topBarViewModel.saveEmployee(it)
                                showQuickWorkerDialog = false
                                navigateTo(BtpDestinationTab.RESOURCES, 0)
                            },
                            onDismiss = { showQuickWorkerDialog = false }
                        )
                    }
                    if (showQuickProjectDialog) {
                        ProjectFormDialog(
                            initial = null,
                            onSave = {
                                topBarViewModel.saveProject(it)
                                showQuickProjectDialog = false
                                navigateTo(BtpDestinationTab.PROJECTS, 0)
                            },
                            onDismiss = { showQuickProjectDialog = false }
                        )
                    }
                    if (showQuickExpenseDialog) {
                        ExpenseFormDialog(
                            initial = null,
                            projectNames = projectsList.map { it.name },
                            onSave = {
                                topBarViewModel.saveExpense(it)
                                showQuickExpenseDialog = false
                                navigateTo(BtpDestinationTab.FINANCE, 0)
                            },
                            onDismiss = { showQuickExpenseDialog = false }
                        )
                    }
                    if (showQuickEquipmentDialog) {
                        EquipmentFormDialog(
                            initial = null,
                            onSave = {
                                topBarViewModel.saveEquipment(it)
                                showQuickEquipmentDialog = false
                                navigateTo(BtpDestinationTab.RESOURCES, 1)
                            },
                            onDismiss = { showQuickEquipmentDialog = false }
                        )
                    }
                    if (showQuickFaultDialog) {
                        FaultReportDialog(
                            equipmentNames = equipmentList.map { it.name },
                            siteNames = sitesList.map { it.name },
                            onSubmit = { eqName, siteName, severity, desc ->
                                topBarViewModel.reportEquipmentFault(eqName, siteName, severity, desc)
                                showQuickFaultDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(strings.topBar.faultRecordedMsg)
                                }
                            },
                            onDismiss = { showQuickFaultDialog = false }
                        )
                    }
                    if (showQuickTaskDialog) {
                        ScheduleTaskFormDialog(
                            initial = null,
                            projectNames = projectsList.map { it.name },
                            onSave = {
                                topBarViewModel.saveTask(it)
                                showQuickTaskDialog = false
                                activeSheet = ActiveTopBarSheet.TASKS
                            },
                            onDismiss = { showQuickTaskDialog = false }
                        )
                    }
                }
            }
        }
    }
}
