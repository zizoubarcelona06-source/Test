package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.RolePermissionPolicy
import com.example.core.security.RolePermissions
import com.example.core.sync.SyncCoordinator
import com.example.core.sync.SyncStatusSnapshot
import com.example.core.util.Formatters
import com.example.data.local.entity.ChatChannelEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.EquipmentEntity
import com.example.data.local.entity.EquipmentFaultEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScheduleTaskEntity
import com.example.data.local.entity.SiteEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.SyncOutboxEntity
import com.example.data.local.entity.UserAccountEntity
import com.example.data.local.entity.UserSessionDeviceEntity
import com.example.data.repository.BtpRepository
import com.example.ui.screens.dashboard.BtpDestinationTab
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GlobalSearchResult(
    val key: String,
    val category: String, // WORKERS, PROJECTS, EQUIPMENT, INVOICES, PARTNERS, CHATS
    val title: String,
    val subtitle: String,
    val badge: String,
    val targetTab: BtpDestinationTab,
    val targetSubTab: Int = 0,
    val openChatChannelId: Long? = null
)

private data class SearchCatalogBundle(
    val projects: List<ProjectEntity>,
    val sites: List<SiteEntity>,
    val employees: List<EmployeeEntity>,
    val equipment: List<EquipmentEntity>,
    val invoices: List<InvoiceEntity>
)

private data class SearchSocialBundle(
    val clients: List<ClientEntity>,
    val suppliers: List<SupplierEntity>,
    val channels: List<ChatChannelEntity>,
    val messages: List<ChatMessageEntity>
)

@OptIn(ExperimentalCoroutinesApi::class)
class TopBarCenterViewModel(
    private val repository: BtpRepository,
    private val syncCoordinator: SyncCoordinator
) : ViewModel() {

    private val _activeUser = MutableStateFlow<UserAccountEntity?>(null)
    val activeUser: StateFlow<UserAccountEntity?> = _activeUser.asStateFlow()

    val permissions: StateFlow<RolePermissions> = _activeUser
        .map { RolePermissionPolicy.permissionsFor(it?.role) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RolePermissionPolicy.permissionsFor(null))

    fun setActiveUser(user: UserAccountEntity?) {
        _activeUser.value = user
    }

    // --- BTP CHAT ---
    private val _selectedChannelId = MutableStateFlow<Long?>(null)
    val selectedChannelId: StateFlow<Long?> = _selectedChannelId.asStateFlow()

    private val _chatFilterType = MutableStateFlow("ALL")
    val chatFilterType: StateFlow<String> = _chatFilterType.asStateFlow()

    private val _chatSearchQuery = MutableStateFlow("")
    val chatSearchQuery: StateFlow<String> = _chatSearchQuery.asStateFlow()

    val allChannels: StateFlow<List<ChatChannelEntity>> = repository.chatChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredChannels: StateFlow<List<ChatChannelEntity>> = combine(
        repository.chatChannels,
        repository.allChatMessages,
        _chatFilterType,
        _chatSearchQuery
    ) { channels, messages, filterType, query ->
        val q = query.trim().lowercase()
        channels.filter { ch ->
            val typeOk = when (filterType) {
                "ALL" -> true
                "DIRECT" -> ch.channelType == "DIRECT"
                "GROUPS" -> ch.channelType == "PROJECT" || ch.channelType == "SITE"
                "ANNOUNCEMENT" -> ch.channelType == "ANNOUNCEMENT"
                else -> true
            }
            val queryOk = if (q.isEmpty()) true else {
                ch.name.lowercase().contains(q) ||
                    ch.projectName.lowercase().contains(q) ||
                    ch.lastMessagePreview.lowercase().contains(q) ||
                    messages.any { m ->
                        m.channelId == ch.id && (
                            m.content.lowercase().contains(q) ||
                                m.attachmentName.lowercase().contains(q) ||
                                m.senderName.lowercase().contains(q)
                            )
                    }
            }
            typeOk && queryOk
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedChannelMessages: StateFlow<List<ChatMessageEntity>> = _selectedChannelId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeChannelMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadChatCount: StateFlow<Int> = repository.chatChannels
        .map { list -> list.sumOf { it.unreadCount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun selectChannel(channelId: Long?) {
        _selectedChannelId.value = channelId
        if (channelId != null) {
            viewModelScope.launch {
                repository.markChannelRead(channelId)
            }
        }
    }

    fun setChatFilterType(type: String) {
        _chatFilterType.value = type
    }

    fun setChatSearchQuery(query: String) {
        _chatSearchQuery.value = query
    }

    fun sendChatMessage(
        content: String,
        attachmentType: String = "NONE",
        attachmentName: String = "",
        attachmentUri: String = ""
    ) {
        val channelId = _selectedChannelId.value ?: return
        val sender = _activeUser.value ?: return
        if (content.isBlank() && attachmentName.isBlank()) return
        viewModelScope.launch {
            repository.sendChatMessage(
                channelId = channelId,
                sender = sender,
                content = content,
                attachmentType = attachmentType,
                attachmentName = attachmentName,
                attachmentUri = attachmentUri
            )
        }
    }

    fun startDirectConversation(colleague: UserAccountEntity) {
        val me = _activeUser.value ?: return
        val existing = allChannels.value.firstOrNull { ch ->
            ch.channelType == "DIRECT" && (
                ch.participantIdsCsv == "${me.id},${colleague.id}" ||
                    ch.participantIdsCsv == "${colleague.id},${me.id}" ||
                    ch.name.contains(colleague.fullName)
                )
        }
        if (existing != null) {
            selectChannel(existing.id)
            return
        }
        viewModelScope.launch {
            val newId = repository.createChatChannel(
                name = "${me.fullName} ↔ ${colleague.fullName}",
                channelType = "DIRECT",
                projectName = colleague.branchName,
                participantIdsCsv = "${me.id},${colleague.id}"
            )
            _selectedChannelId.value = newId
        }
    }

    fun createGroupChannel(name: String, channelType: String, projectName: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newId = repository.createChatChannel(
                name = name,
                channelType = channelType,
                projectName = projectName,
                participantIdsCsv = ""
            )
            _selectedChannelId.value = newId
        }
    }

    fun markAllChannelsRead() {
        viewModelScope.launch { repository.markAllChannelsRead() }
    }

    // --- NOTIFICATIONS ---
    val notificationPreferences: StateFlow<Map<String, Boolean>> = _activeUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else repository.observeNotificationPreferences(user.id)
        }
        .map { prefs ->
            val map = RolePermissionPolicy.allNotificationCategories.associateWith { true }.toMutableMap()
            prefs.forEach { map[it.category] = it.enabled }
            map.toMap()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            RolePermissionPolicy.allNotificationCategories.associateWith { true }
        )

    val visibleNotifications: StateFlow<List<NotificationEntity>> = combine(
        repository.notifications,
        permissions,
        notificationPreferences
    ) { items, perms, prefs ->
        items.filter { n ->
            n.category in perms.allowedNotificationCategories &&
                (prefs[n.category] ?: true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = visibleNotifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markNotificationRead(id: Long) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsRead() }
    }

    fun toggleNotificationPreference(category: String, enabled: Boolean) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            repository.setNotificationPreference(user.id, category, enabled)
        }
    }

    // --- USER DEVICES ---
    val activeUserDevices: StateFlow<List<UserSessionDeviceEntity>> = _activeUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else repository.observeUserDevices(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun revokeDeviceSession(deviceId: Long) {
        viewModelScope.launch { repository.revokeDeviceSession(deviceId) }
    }

    // --- GLOBAL SEARCH ---
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    private val _globalSearchCategory = MutableStateFlow("ALL")
    val globalSearchCategory: StateFlow<String> = _globalSearchCategory.asStateFlow()

    private val searchCatalogFlow = combine(
        repository.projects,
        repository.sites,
        repository.employees,
        repository.equipment,
        repository.invoices
    ) { projects, sites, employees, equipment, invoices ->
        SearchCatalogBundle(projects, sites, employees, equipment, invoices)
    }

    private val searchSocialFlow = combine(
        repository.clients,
        repository.suppliers,
        repository.chatChannels,
        repository.allChatMessages
    ) { clients, suppliers, channels, messages ->
        SearchSocialBundle(clients, suppliers, channels, messages)
    }

    val globalSearchResults: StateFlow<List<GlobalSearchResult>> = combine(
        searchCatalogFlow,
        searchSocialFlow,
        permissions,
        _globalSearchQuery,
        _globalSearchCategory
    ) { catalog, social, perms, query, categoryFilter ->
        val q = query.trim().lowercase()
        val results = mutableListOf<GlobalSearchResult>()

        fun matches(vararg fields: String): Boolean {
            if (q.isEmpty()) return true
            return fields.any { it.lowercase().contains(q) }
        }

        if (categoryFilter == "ALL" || categoryFilter == "WORKERS") {
            catalog.employees.filter {
                matches(it.fullName, it.role, it.assignedSiteName, it.phone)
            }.forEach { emp ->
                results += GlobalSearchResult(
                    key = "emp_${emp.id}",
                    category = "WORKERS",
                    title = emp.fullName,
                    subtitle = "${emp.role} • ${emp.assignedSiteName}",
                    badge = emp.attendanceStatus,
                    targetTab = BtpDestinationTab.RESOURCES,
                    targetSubTab = 0
                )
            }
        }

        if (categoryFilter == "ALL" || categoryFilter == "PROJECTS") {
            catalog.projects.filter {
                matches(it.name, it.code, it.clientName, it.location)
            }.forEach { p ->
                results += GlobalSearchResult(
                    key = "prj_${p.id}",
                    category = "PROJECTS",
                    title = p.name,
                    subtitle = "${p.code} • ${p.location}",
                    badge = "${p.progressPercent}%",
                    targetTab = BtpDestinationTab.PROJECTS,
                    targetSubTab = 0
                )
            }
            catalog.sites.filter {
                matches(it.name, it.projectName, it.supervisorName, it.address)
            }.forEach { s ->
                results += GlobalSearchResult(
                    key = "site_${s.id}",
                    category = "PROJECTS",
                    title = s.name,
                    subtitle = "${s.projectName} • ${s.supervisorName}",
                    badge = s.status,
                    targetTab = BtpDestinationTab.PROJECTS,
                    targetSubTab = 1
                )
            }
        }

        if (categoryFilter == "ALL" || categoryFilter == "EQUIPMENT") {
            catalog.equipment.filter {
                matches(it.name, it.code, it.category, it.assignedSiteName)
            }.forEach { eq ->
                results += GlobalSearchResult(
                    key = "eq_${eq.id}",
                    category = "EQUIPMENT",
                    title = eq.name,
                    subtitle = "${eq.code} • ${eq.assignedSiteName}",
                    badge = eq.status,
                    targetTab = BtpDestinationTab.RESOURCES,
                    targetSubTab = 1
                )
            }
        }

        if (perms.canViewFinance && (categoryFilter == "ALL" || categoryFilter == "INVOICES")) {
            catalog.invoices.filter {
                matches(it.invoiceNumber, it.partyName, it.projectName, it.status)
            }.forEach { inv ->
                results += GlobalSearchResult(
                    key = "inv_${inv.id}",
                    category = "INVOICES",
                    title = "${inv.invoiceNumber} — ${inv.partyName}",
                    subtitle = inv.projectName,
                    badge = inv.status,
                    targetTab = BtpDestinationTab.FINANCE,
                    targetSubTab = 1
                )
            }
        }

        if (categoryFilter == "ALL" || categoryFilter == "PARTNERS") {
            social.clients.filter {
                matches(it.name, it.companyName, it.phone, it.address)
            }.forEach { c ->
                results += GlobalSearchResult(
                    key = "cli_${c.id}",
                    category = "PARTNERS",
                    title = c.name,
                    subtitle = "${c.companyName} • ${c.phone}",
                    badge = "Client",
                    targetTab = BtpDestinationTab.FINANCE,
                    targetSubTab = 2
                )
            }
            social.suppliers.filter {
                matches(it.name, it.category, it.phone, it.address)
            }.forEach { s ->
                results += GlobalSearchResult(
                    key = "sup_${s.id}",
                    category = "PARTNERS",
                    title = s.name,
                    subtitle = "${s.category} • ${s.phone}",
                    badge = "Supplier",
                    targetTab = BtpDestinationTab.FINANCE,
                    targetSubTab = 2
                )
            }
        }

        if (categoryFilter == "ALL" || categoryFilter == "CHATS") {
            social.channels.filter {
                matches(it.name, it.projectName, it.lastMessagePreview)
            }.forEach { ch ->
                results += GlobalSearchResult(
                    key = "ch_${ch.id}",
                    category = "CHATS",
                    title = ch.name,
                    subtitle = ch.lastMessagePreview.ifBlank { ch.projectName },
                    badge = ch.channelType,
                    targetTab = BtpDestinationTab.DASHBOARD,
                    openChatChannelId = ch.id
                )
            }
            if (q.isNotEmpty()) {
                social.messages.filter {
                    it.attachmentName.lowercase().contains(q) || it.content.lowercase().contains(q)
                }.take(10).forEach { m ->
                    val chTitle = social.channels.firstOrNull { it.id == m.channelId }?.name ?: "BTP Chat"
                    results += GlobalSearchResult(
                        key = "msg_${m.id}",
                        category = "CHATS",
                        title = m.attachmentName.ifBlank { m.content.take(45) },
                        subtitle = "${m.senderName} • $chTitle",
                        badge = if (m.attachmentType != "NONE") m.attachmentType else "MSG",
                        targetTab = BtpDestinationTab.DASHBOARD,
                        openChatChannelId = m.channelId
                    )
                }
            }
        }

        if (q.isEmpty()) results.take(20) else results.take(40)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setGlobalSearchQuery(query: String) {
        _globalSearchQuery.value = query
    }

    fun setGlobalSearchCategory(category: String) {
        _globalSearchCategory.value = category
    }

    // --- QUICK ACTIONS & FAULTS ---
    val projectsList: StateFlow<List<ProjectEntity>> = repository.projects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sitesList: StateFlow<List<SiteEntity>> = repository.sites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val equipmentList: StateFlow<List<EquipmentEntity>> = repository.equipment
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val equipmentFaults: StateFlow<List<EquipmentFaultEntity>> = repository.equipmentFaults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveEmployee(emp: EmployeeEntity) = viewModelScope.launch { repository.saveEmployee(emp) }
    fun saveProject(project: ProjectEntity) = viewModelScope.launch { repository.saveProject(project) }
    fun saveExpense(expense: ExpenseEntity) = viewModelScope.launch { repository.saveExpense(expense) }
    fun saveEquipment(item: EquipmentEntity) = viewModelScope.launch { repository.saveEquipment(item) }
    fun saveTask(task: ScheduleTaskEntity) = viewModelScope.launch {
        val activeId = _activeUser.value?.id
        val normalized = if (task.id == 0L && task.assignedToUserId == null) {
            task.copy(assignedToUserId = activeId)
        } else task
        repository.saveScheduleTask(normalized)
    }

    fun reportEquipmentFault(
        equipmentName: String,
        siteName: String,
        severity: String,
        description: String
    ) {
        val reporter = _activeUser.value ?: return
        if (equipmentName.isBlank() || description.isBlank()) return
        viewModelScope.launch {
            repository.reportEquipmentFault(equipmentName, siteName, severity, description, reporter)
        }
    }

    // --- TASKS CENTER ---
    val tasks: StateFlow<List<ScheduleTaskEntity>> = repository.scheduleTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingOrOverdueTasksCount: StateFlow<Int> = tasks
        .map { list ->
            val today = Formatters.todayDate()
            list.count { !it.isCompleted && (it.dueDate < today || it.assignedToUserId == _activeUser.value?.id) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun updateTaskProgress(task: ScheduleTaskEntity, progress: Int) {
        viewModelScope.launch {
            repository.updateScheduleTaskProgress(task, progress)
        }
    }

    fun toggleTaskComplete(task: ScheduleTaskEntity) {
        val newComplete = !task.isCompleted
        val newProgress = if (newComplete) 100 else task.progress.coerceAtMost(90)
        viewModelScope.launch {
            repository.saveScheduleTask(task.copy(isCompleted = newComplete, progress = newProgress))
        }
    }

    // --- SYNC STATUS ---
    val syncSnapshot: StateFlow<SyncStatusSnapshot> = syncCoordinator.snapshot
    val syncOutbox: StateFlow<List<SyncOutboxEntity>> = repository.syncOutbox
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun syncNow() {
        syncCoordinator.triggerAutoSync(force = true)
    }

    fun retryFailedSync() {
        syncCoordinator.triggerAutoSync(force = true)
    }

    fun clearSyncedOutbox() {
        viewModelScope.launch {
            syncCoordinator.clearSyncedHistory()
        }
    }
}
