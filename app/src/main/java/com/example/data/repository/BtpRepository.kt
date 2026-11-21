package com.example.data.repository

import com.example.core.security.SecurityUtils
import com.example.data.local.BtpDatabase
import com.example.data.local.DatabaseSeeder
import com.example.data.local.entity.ChatChannelEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.EquipmentEntity
import com.example.data.local.entity.EquipmentFaultEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.MaterialEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.NotificationPreferenceEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScheduleTaskEntity
import com.example.data.local.entity.SiteEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.SyncOutboxEntity
import com.example.data.local.entity.UserAccountEntity
import com.example.data.local.entity.UserSessionDeviceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

sealed class AuthResult {
    data class Success(val user: UserAccountEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class BtpRepository(
    private val db: BtpDatabase,
    var onOutboxChanged: (() -> Unit)? = null
) {

    // Initialization / Seeding
    suspend fun initializeOfflineData() {
        DatabaseSeeder.ensureInitialDataSeeded(db)
    }

    suspend fun resetSampleData() {
        DatabaseSeeder.resetAndSeedSampleData(db)
    }

    // Users & Auth
    val users: Flow<List<UserAccountEntity>> = db.userAccountDao().observeAll()

    suspend fun authenticate(phoneOrUsername: String, password: String): AuthResult {
        DatabaseSeeder.ensureUsersSeeded(db)
        val normalized = phoneOrUsername.trim().replace(" ", "").replace("-", "")
        val account = db.userAccountDao().getByPhoneOrUsername(normalized)
            ?: return AuthResult.Error("رقم الهاتف أو كلمة المرور غير صحيحة")
        if (!account.isActive) {
            return AuthResult.Error("هذا الحساب معطل حالياً، يرجى مراجعة المدير")
        }
        val valid = SecurityUtils.verifyPassword(password, account.salt, account.passwordHash)
        if (!valid) {
            return AuthResult.Error("رقم الهاتف أو كلمة المرور غير صحيحة")
        }
        val updated = account.copy(lastLoginAt = System.currentTimeMillis())
        db.userAccountDao().update(updated)
        return AuthResult.Success(updated)
    }

    suspend fun getUserById(id: Long): UserAccountEntity? = db.userAccountDao().getById(id)

    suspend fun createUser(
        fullName: String,
        username: String,
        phone: String,
        role: String,
        password: String
    ): Result<Long> {
        val cleanUsername = username.trim()
        val cleanPhone = phone.trim().replace(" ", "").replace("-", "")
        if (db.userAccountDao().getByPhoneOrUsername(cleanPhone) != null ||
            db.userAccountDao().getByUsername(cleanUsername) != null
        ) {
            return Result.failure(IllegalArgumentException("رقم الهاتف أو اسم المستخدم مسجل مسبقاً"))
        }
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(password, salt)
        val id = db.userAccountDao().insert(
            UserAccountEntity(
                fullName = fullName.trim(),
                username = cleanUsername,
                phone = cleanPhone,
                role = role,
                passwordHash = hash,
                salt = salt,
                isActive = true
            )
        )
        recordSyncOperation("USER", id, "CREATE", "${fullName.trim()} ($role)")
        return Result.success(id)
    }

    suspend fun changePassword(userId: Long, newPassword: String) {
        val user = db.userAccountDao().getById(userId) ?: return
        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPassword, newSalt)
        db.userAccountDao().update(user.copy(passwordHash = newHash, salt = newSalt))
        recordSyncOperation("USER_SECURITY", userId, "UPDATE", user.username)
    }

    suspend fun verifyAndChangePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        val user = db.userAccountDao().getById(userId)
            ?: return Result.failure(IllegalStateException("User not found"))
        if (!SecurityUtils.verifyPassword(currentPassword, user.salt, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("ERR_WRONG_CURRENT_PASSWORD"))
        }
        if (newPassword.length < 4) {
            return Result.failure(IllegalArgumentException("ERR_SHORT_PASSWORD"))
        }
        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPassword, newSalt)
        db.userAccountDao().update(user.copy(passwordHash = newHash, salt = newSalt))
        recordSyncOperation("USER_SECURITY", userId, "UPDATE", user.username)
        return Result.success(Unit)
    }

    suspend fun updateUserLanguage(userId: Long, langCode: String): UserAccountEntity? {
        db.userAccountDao().updatePreferredLanguage(userId, langCode)
        recordSyncOperation("USER_PREFS", userId, "UPDATE", "lang=$langCode")
        return db.userAccountDao().getById(userId)
    }

    suspend fun updateUserProfile(
        userId: Long,
        fullName: String,
        branchName: String,
        avatarUri: String
    ): UserAccountEntity? {
        db.userAccountDao().updateProfile(userId, fullName.trim(), branchName.trim(), avatarUri)
        recordSyncOperation("USER_PROFILE", userId, "UPDATE", fullName.trim())
        return db.userAccountDao().getById(userId)
    }

    suspend fun toggleUserActive(user: UserAccountEntity) {
        db.userAccountDao().update(user.copy(isActive = !user.isActive))
        recordSyncOperation("USER", user.id, "UPDATE", "${user.username}: active=${!user.isActive}")
    }

    // Active Devices / Sessions
    fun observeUserDevices(userId: Long): Flow<List<UserSessionDeviceEntity>> =
        db.userSessionDeviceDao().observeForUser(userId)

    suspend fun registerCurrentDeviceSession(
        userId: Long,
        deviceName: String,
        deviceModel: String
    ) {
        val count = db.userSessionDeviceDao().countForUser(userId)
        if (count == 0) {
            db.userSessionDeviceDao().insert(
                UserSessionDeviceEntity(
                    userId = userId,
                    deviceName = deviceName,
                    deviceModel = deviceModel,
                    locationLabel = "المقر / الميدان",
                    lastActiveAt = System.currentTimeMillis(),
                    isCurrentDevice = true,
                    isActive = true
                )
            )
            db.userSessionDeviceDao().insert(
                UserSessionDeviceEntity(
                    userId = userId,
                    deviceName = "Samsung Galaxy Tab S9 (المكتب)",
                    deviceModel = "Tablet • Android 14",
                    locationLabel = "المكتب الهندسي",
                    lastActiveAt = System.currentTimeMillis() - 86_400_000L,
                    isCurrentDevice = false,
                    isActive = true
                )
            )
        }
    }

    suspend fun revokeDeviceSession(deviceId: Long) {
        db.userSessionDeviceDao().revokeDevice(deviceId)
        recordSyncOperation("DEVICE_SESSION", deviceId, "UPDATE", "Revoke device #$deviceId")
    }

    // Projects
    val projects: Flow<List<ProjectEntity>> = db.projectDao().observeAll()
    suspend fun saveProject(project: ProjectEntity) {
        val isCreate = project.id == 0L
        val id = if (isCreate) db.projectDao().insert(project) else {
            db.projectDao().update(project)
            project.id
        }
        recordSyncOperation("PROJECT", id, if (isCreate) "CREATE" else "UPDATE", project.name)
    }
    suspend fun deleteProject(project: ProjectEntity) {
        db.projectDao().delete(project)
        recordSyncOperation("PROJECT", project.id, "DELETE", project.name)
    }

    // Sites
    val sites: Flow<List<SiteEntity>> = db.siteDao().observeAll()
    suspend fun saveSite(site: SiteEntity) {
        val isCreate = site.id == 0L
        val id = if (isCreate) db.siteDao().insert(site) else {
            db.siteDao().update(site)
            site.id
        }
        recordSyncOperation("SITE", id, if (isCreate) "CREATE" else "UPDATE", site.name)
    }
    suspend fun deleteSite(site: SiteEntity) {
        db.siteDao().delete(site)
        recordSyncOperation("SITE", site.id, "DELETE", site.name)
    }

    // Schedule Tasks
    val scheduleTasks: Flow<List<ScheduleTaskEntity>> = db.scheduleTaskDao().observeAll()
    suspend fun saveScheduleTask(task: ScheduleTaskEntity) {
        val isCreate = task.id == 0L
        val id = if (isCreate) db.scheduleTaskDao().insert(task) else {
            db.scheduleTaskDao().update(task)
            task.id
        }
        recordSyncOperation("TASK", id, if (isCreate) "CREATE" else "UPDATE", task.title)
        if (isCreate) {
            db.notificationDao().insert(
                NotificationEntity(
                    category = "TASKS",
                    title = task.title,
                    message = "${task.projectName} • ${task.dueDate}",
                    targetTab = "PROJECTS",
                    targetSubTab = 2,
                    relatedEntityId = id,
                    isRead = false,
                    audience = "ALL"
                )
            )
        }
    }
    suspend fun updateScheduleTaskProgress(task: ScheduleTaskEntity, newProgress: Int) {
        val clamped = newProgress.coerceIn(0, 100)
        val completed = clamped >= 100
        db.scheduleTaskDao().update(
            task.copy(progress = clamped, isCompleted = completed)
        )
        recordSyncOperation("TASK", task.id, "UPDATE", "${task.title} ($clamped%)")
    }
    suspend fun deleteScheduleTask(task: ScheduleTaskEntity) {
        db.scheduleTaskDao().delete(task)
        recordSyncOperation("TASK", task.id, "DELETE", task.title)
    }

    // Employees
    val employees: Flow<List<EmployeeEntity>> = db.employeeDao().observeAll()
    suspend fun saveEmployee(employee: EmployeeEntity) {
        val isCreate = employee.id == 0L
        val id = if (isCreate) db.employeeDao().insert(employee) else {
            db.employeeDao().update(employee)
            employee.id
        }
        recordSyncOperation("EMPLOYEE", id, if (isCreate) "CREATE" else "UPDATE", employee.fullName)
    }
    suspend fun deleteEmployee(employee: EmployeeEntity) {
        db.employeeDao().delete(employee)
        recordSyncOperation("EMPLOYEE", employee.id, "DELETE", employee.fullName)
    }

    // Equipment & Fault Reporting
    val equipment: Flow<List<EquipmentEntity>> = db.equipmentDao().observeAll()
    val equipmentFaults: Flow<List<EquipmentFaultEntity>> = db.equipmentFaultDao().observeAll()

    suspend fun saveEquipment(item: EquipmentEntity) {
        val isCreate = item.id == 0L
        val id = if (isCreate) db.equipmentDao().insert(item) else {
            db.equipmentDao().update(item)
            item.id
        }
        recordSyncOperation("EQUIPMENT", id, if (isCreate) "CREATE" else "UPDATE", item.name)
    }
    suspend fun deleteEquipment(item: EquipmentEntity) {
        db.equipmentDao().delete(item)
        recordSyncOperation("EQUIPMENT", item.id, "DELETE", item.name)
    }

    suspend fun reportEquipmentFault(
        equipmentName: String,
        siteName: String,
        severity: String,
        description: String,
        reporter: UserAccountEntity
    ): Long {
        val faultId = db.equipmentFaultDao().insert(
            EquipmentFaultEntity(
                equipmentName = equipmentName.trim(),
                siteName = siteName.trim(),
                severity = severity,
                description = description.trim(),
                reportedByUserId = reporter.id,
                reportedByName = reporter.fullName
            )
        )
        // Put matching equipment into maintenance status automatically
        val allEquipment = db.equipmentDao().observeAll().first()
        allEquipment.firstOrNull {
            it.name.equals(equipmentName.trim(), ignoreCase = true) ||
                equipmentName.contains(it.code, ignoreCase = true)
        }?.let { eq ->
            db.equipmentDao().update(eq.copy(status = "صيانة"))
        }
        db.notificationDao().insert(
            NotificationEntity(
                category = "EQUIPMENT",
                title = "${equipmentName.trim()} ($severity)",
                message = "${reporter.fullName}: ${description.trim()}",
                targetTab = "RESOURCES",
                targetSubTab = 1,
                relatedEntityId = faultId,
                isRead = false,
                audience = "ALL"
            )
        )
        recordSyncOperation("EQUIPMENT_FAULT", faultId, "CREATE", "${equipmentName.trim()} - $severity")
        return faultId
    }

    // Materials
    val materials: Flow<List<MaterialEntity>> = db.materialDao().observeAll()
    suspend fun saveMaterial(material: MaterialEntity) {
        val isCreate = material.id == 0L
        val id = if (isCreate) db.materialDao().insert(material) else {
            db.materialDao().update(material)
            material.id
        }
        recordSyncOperation("MATERIAL", id, if (isCreate) "CREATE" else "UPDATE", material.name)
    }
    suspend fun deleteMaterial(material: MaterialEntity) {
        db.materialDao().delete(material)
        recordSyncOperation("MATERIAL", material.id, "DELETE", material.name)
    }

    // Expenses
    val expenses: Flow<List<ExpenseEntity>> = db.expenseDao().observeAll()
    suspend fun saveExpense(expense: ExpenseEntity) {
        val isCreate = expense.id == 0L
        val id = if (isCreate) db.expenseDao().insert(expense) else {
            db.expenseDao().update(expense)
            expense.id
        }
        recordSyncOperation("EXPENSE", id, if (isCreate) "CREATE" else "UPDATE", expense.title)
    }
    suspend fun deleteExpense(expense: ExpenseEntity) {
        db.expenseDao().delete(expense)
        recordSyncOperation("EXPENSE", expense.id, "DELETE", expense.title)
    }

    // Invoices
    val invoices: Flow<List<InvoiceEntity>> = db.invoiceDao().observeAll()
    suspend fun saveInvoice(invoice: InvoiceEntity) {
        val isCreate = invoice.id == 0L
        val id = if (isCreate) db.invoiceDao().insert(invoice) else {
            db.invoiceDao().update(invoice)
            invoice.id
        }
        recordSyncOperation("INVOICE", id, if (isCreate) "CREATE" else "UPDATE", invoice.invoiceNumber)
    }
    suspend fun recordInvoicePayment(invoice: InvoiceEntity, addedAmount: Double) {
        val newPaid = (invoice.paidAmount + addedAmount).coerceIn(0.0, invoice.totalAmount)
        val newStatus = when {
            newPaid >= invoice.totalAmount -> "مدفوعة"
            newPaid > 0.0 -> "مدفوعة جزئياً"
            else -> "غير مدفوعة"
        }
        db.invoiceDao().update(invoice.copy(paidAmount = newPaid, status = newStatus))
        recordSyncOperation("INVOICE", invoice.id, "UPDATE", "${invoice.invoiceNumber} ($newStatus)")
    }
    suspend fun deleteInvoice(invoice: InvoiceEntity) {
        db.invoiceDao().delete(invoice)
        recordSyncOperation("INVOICE", invoice.id, "DELETE", invoice.invoiceNumber)
    }

    // Clients
    val clients: Flow<List<ClientEntity>> = db.clientDao().observeAll()
    suspend fun saveClient(client: ClientEntity) {
        val isCreate = client.id == 0L
        val id = if (isCreate) db.clientDao().insert(client) else {
            db.clientDao().update(client)
            client.id
        }
        recordSyncOperation("CLIENT", id, if (isCreate) "CREATE" else "UPDATE", client.name)
    }
    suspend fun deleteClient(client: ClientEntity) {
        db.clientDao().delete(client)
        recordSyncOperation("CLIENT", client.id, "DELETE", client.name)
    }

    // Suppliers
    val suppliers: Flow<List<SupplierEntity>> = db.supplierDao().observeAll()
    suspend fun saveSupplier(supplier: SupplierEntity) {
        val isCreate = supplier.id == 0L
        val id = if (isCreate) db.supplierDao().insert(supplier) else {
            db.supplierDao().update(supplier)
            supplier.id
        }
        recordSyncOperation("SUPPLIER", id, if (isCreate) "CREATE" else "UPDATE", supplier.name)
    }
    suspend fun deleteSupplier(supplier: SupplierEntity) {
        db.supplierDao().delete(supplier)
        recordSyncOperation("SUPPLIER", supplier.id, "DELETE", supplier.name)
    }

    // BTP Chat
    val chatChannels: Flow<List<ChatChannelEntity>> = db.chatChannelDao().observeAll()
    val allChatMessages: Flow<List<ChatMessageEntity>> = db.chatMessageDao().observeAll()

    fun observeChannelMessages(channelId: Long): Flow<List<ChatMessageEntity>> =
        db.chatMessageDao().observeForChannel(channelId)

    suspend fun sendChatMessage(
        channelId: Long,
        sender: UserAccountEntity,
        content: String,
        attachmentType: String = "NONE",
        attachmentName: String = "",
        attachmentUri: String = ""
    ): Long {
        val now = System.currentTimeMillis()
        val preview = when {
            content.isNotBlank() -> content.trim()
            attachmentName.isNotBlank() -> attachmentName
            else -> attachmentType
        }
        val msgId = db.chatMessageDao().insert(
            ChatMessageEntity(
                channelId = channelId,
                senderUserId = sender.id,
                senderName = sender.fullName,
                senderRole = sender.role,
                content = content.trim(),
                attachmentType = attachmentType,
                attachmentName = attachmentName,
                attachmentUri = attachmentUri,
                timestamp = now,
                isRead = true,
                syncStatus = "PENDING"
            )
        )
        db.chatChannelDao().updateLastMessage(
            channelId = channelId,
            preview = preview,
            timestamp = now,
            unreadCount = 0
        )
        recordSyncOperation("CHAT_MESSAGE", msgId, "MESSAGE", "${sender.fullName}: ${preview.take(40)}")
        return msgId
    }

    suspend fun createChatChannel(
        name: String,
        channelType: String,
        projectName: String = "",
        participantIdsCsv: String = ""
    ): Long {
        val id = db.chatChannelDao().insert(
            ChatChannelEntity(
                name = name.trim(),
                channelType = channelType,
                projectName = projectName.trim(),
                participantIdsCsv = participantIdsCsv,
                lastMessagePreview = "",
                lastMessageAt = System.currentTimeMillis(),
                unreadCount = 0
            )
        )
        recordSyncOperation("CHAT_CHANNEL", id, "CREATE", name.trim())
        return id
    }

    suspend fun markChannelRead(channelId: Long) {
        db.chatChannelDao().markChannelRead(channelId)
        db.chatMessageDao().markChannelMessagesRead(channelId)
    }

    suspend fun markAllChannelsRead() {
        db.chatChannelDao().markAllRead()
        db.chatMessageDao().markAllMessagesRead()
    }

    // Notifications & Preferences
    val notifications: Flow<List<NotificationEntity>> = db.notificationDao().observeAll()

    fun observeNotificationPreferences(userId: Long): Flow<List<NotificationPreferenceEntity>> =
        db.notificationPreferenceDao().observeForUser(userId)

    suspend fun markNotificationRead(id: Long) {
        db.notificationDao().markRead(id)
    }

    suspend fun markAllNotificationsRead() {
        db.notificationDao().markAllRead()
    }

    suspend fun setNotificationPreference(userId: Long, category: String, enabled: Boolean) {
        val existing = db.notificationPreferenceDao().getPreference(userId, category)
        if (existing == null) {
            db.notificationPreferenceDao().insert(
                NotificationPreferenceEntity(
                    userId = userId,
                    category = category,
                    enabled = enabled
                )
            )
        } else {
            db.notificationPreferenceDao().update(existing.copy(enabled = enabled))
        }
        recordSyncOperation("NOTIF_PREF", userId, "UPDATE", "$category=$enabled")
    }

    // Sync Outbox
    val syncOutbox: Flow<List<SyncOutboxEntity>> = db.syncOutboxDao().observeAll()

    suspend fun recordSyncOperation(
        entityType: String,
        entityId: Long,
        operation: String,
        summary: String
    ) {
        db.syncOutboxDao().insert(
            SyncOutboxEntity(
                entityType = entityType,
                entityId = entityId,
                operation = operation,
                payloadSummary = summary,
                status = "PENDING"
            )
        )
        onOutboxChanged?.invoke()
    }
}
