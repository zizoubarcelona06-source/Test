package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    fun observeAll(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE REPLACE(REPLACE(phone, ' ', ''), '-', '') = :identifier OR LOWER(username) = LOWER(:identifier) LIMIT 1")
    suspend fun getByPhoneOrUsername(identifier: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserAccountEntity?

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserAccountEntity): Long

    @Update
    suspend fun update(user: UserAccountEntity)

    @Query("UPDATE user_accounts SET preferredLanguageCode = :langCode WHERE id = :userId")
    suspend fun updatePreferredLanguage(userId: Long, langCode: String)

    @Query("UPDATE user_accounts SET fullName = :fullName, branchName = :branchName, avatarUri = :avatarUri WHERE id = :userId")
    suspend fun updateProfile(userId: Long, fullName: String, branchName: String, avatarUri: String)

    @Delete
    suspend fun delete(user: UserAccountEntity)
}

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY id DESC")
    fun observeAll(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ClientEntity?

    @Query("SELECT * FROM clients ORDER BY id ASC")
    suspend fun getAll(): List<ClientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: ClientEntity): Long

    @Update
    suspend fun update(client: ClientEntity)

    @Delete
    suspend fun delete(client: ClientEntity)

    @Query("DELETE FROM clients")
    suspend fun deleteAll()
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY id DESC")
    fun observeAll(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SupplierEntity?

    @Query("SELECT * FROM suppliers ORDER BY id ASC")
    suspend fun getAll(): List<SupplierEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: SupplierEntity): Long

    @Update
    suspend fun update(supplier: SupplierEntity)

    @Delete
    suspend fun delete(supplier: SupplierEntity)

    @Query("DELETE FROM suppliers")
    suspend fun deleteAll()
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY id DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProjectEntity?

    @Query("SELECT * FROM projects ORDER BY id ASC")
    suspend fun getAll(): List<ProjectEntity>

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)

    @Query("DELETE FROM projects")
    suspend fun deleteAll()
}

@Dao
interface SiteDao {
    @Query("SELECT * FROM sites ORDER BY id DESC")
    fun observeAll(): Flow<List<SiteEntity>>

    @Query("SELECT * FROM sites WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SiteEntity?

    @Query("SELECT * FROM sites ORDER BY id ASC")
    suspend fun getAll(): List<SiteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(site: SiteEntity): Long

    @Update
    suspend fun update(site: SiteEntity)

    @Delete
    suspend fun delete(site: SiteEntity)

    @Query("DELETE FROM sites")
    suspend fun deleteAll()
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY id DESC")
    fun observeAll(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EmployeeEntity?

    @Query("SELECT * FROM employees ORDER BY id ASC")
    suspend fun getAll(): List<EmployeeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(employee: EmployeeEntity): Long

    @Update
    suspend fun update(employee: EmployeeEntity)

    @Delete
    suspend fun delete(employee: EmployeeEntity)

    @Query("DELETE FROM employees")
    suspend fun deleteAll()
}

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment ORDER BY id DESC")
    fun observeAll(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EquipmentEntity?

    @Query("SELECT * FROM equipment ORDER BY id ASC")
    suspend fun getAll(): List<EquipmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipment: EquipmentEntity): Long

    @Update
    suspend fun update(equipment: EquipmentEntity)

    @Delete
    suspend fun delete(equipment: EquipmentEntity)

    @Query("DELETE FROM equipment")
    suspend fun deleteAll()
}

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials ORDER BY id DESC")
    fun observeAll(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MaterialEntity?

    @Query("SELECT * FROM materials ORDER BY id ASC")
    suspend fun getAll(): List<MaterialEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(material: MaterialEntity): Long

    @Update
    suspend fun update(material: MaterialEntity)

    @Delete
    suspend fun delete(material: MaterialEntity)

    @Query("DELETE FROM materials")
    suspend fun deleteAll()
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses ORDER BY id ASC")
    suspend fun getAll(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY issueDate DESC, id DESC")
    fun observeAll(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): InvoiceEntity?

    @Query("SELECT * FROM invoices ORDER BY id ASC")
    suspend fun getAll(): List<InvoiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: InvoiceEntity): Long

    @Update
    suspend fun update(invoice: InvoiceEntity)

    @Delete
    suspend fun delete(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices")
    suspend fun deleteAll()
}

@Dao
interface ScheduleTaskDao {
    @Query("SELECT * FROM schedule_tasks ORDER BY isCompleted ASC, dueDate ASC, id DESC")
    fun observeAll(): Flow<List<ScheduleTaskEntity>>

    @Query("SELECT * FROM schedule_tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ScheduleTaskEntity?

    @Query("SELECT * FROM schedule_tasks ORDER BY id ASC")
    suspend fun getAll(): List<ScheduleTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: ScheduleTaskEntity): Long

    @Update
    suspend fun update(task: ScheduleTaskEntity)

    @Delete
    suspend fun delete(task: ScheduleTaskEntity)

    @Query("DELETE FROM schedule_tasks")
    suspend fun deleteAll()
}

@Dao
interface ChatChannelDao {
    @Query("SELECT * FROM chat_channels ORDER BY lastMessageAt DESC, id DESC")
    fun observeAll(): Flow<List<ChatChannelEntity>>

    @Query("SELECT * FROM chat_channels WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ChatChannelEntity?

    @Query("SELECT * FROM chat_channels ORDER BY id ASC")
    suspend fun getAll(): List<ChatChannelEntity>

    @Query("SELECT COUNT(*) FROM chat_channels")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channel: ChatChannelEntity): Long

    @Update
    suspend fun update(channel: ChatChannelEntity)

    @Query("UPDATE chat_channels SET unreadCount = 0 WHERE id = :channelId")
    suspend fun markChannelRead(channelId: Long)

    @Query("UPDATE chat_channels SET unreadCount = 0")
    suspend fun markAllRead()

    @Query("UPDATE chat_channels SET lastMessagePreview = :preview, lastMessageAt = :timestamp, unreadCount = :unreadCount WHERE id = :channelId")
    suspend fun updateLastMessage(channelId: Long, preview: String, timestamp: Long, unreadCount: Int)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE channelId = :channelId ORDER BY timestamp ASC, id ASC")
    fun observeForChannel(channelId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ChatMessageEntity?

    @Query("SELECT * FROM chat_messages ORDER BY id ASC")
    suspend fun getAll(): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity): Long

    @Update
    suspend fun update(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET isRead = 1 WHERE channelId = :channelId")
    suspend fun markChannelMessagesRead(channelId: Long)

    @Query("UPDATE chat_messages SET isRead = 1")
    suspend fun markAllMessagesRead()

    @Query("UPDATE chat_messages SET syncStatus = :status WHERE id = :messageId")
    suspend fun updateSyncStatus(messageId: Long, status: String)

    @Query("UPDATE chat_messages SET syncStatus = 'SYNCED' WHERE syncStatus != 'SYNCED'")
    suspend fun markAllSynced()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NotificationEntity?

    @Query("SELECT * FROM notifications ORDER BY id ASC")
    suspend fun getAll(): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity): Long

    @Update
    suspend fun update(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllRead()
}

@Dao
interface NotificationPreferenceDao {
    @Query("SELECT * FROM notification_preferences WHERE userId = :userId")
    fun observeForUser(userId: Long): Flow<List<NotificationPreferenceEntity>>

    @Query("SELECT * FROM notification_preferences WHERE userId = :userId AND category = :category LIMIT 1")
    suspend fun getPreference(userId: Long, category: String): NotificationPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pref: NotificationPreferenceEntity): Long

    @Update
    suspend fun update(pref: NotificationPreferenceEntity)
}

@Dao
interface UserSessionDeviceDao {
    @Query("SELECT * FROM user_session_devices WHERE userId = :userId ORDER BY isCurrentDevice DESC, lastActiveAt DESC")
    fun observeForUser(userId: Long): Flow<List<UserSessionDeviceEntity>>

    @Query("SELECT COUNT(*) FROM user_session_devices WHERE userId = :userId")
    suspend fun countForUser(userId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: UserSessionDeviceEntity): Long

    @Query("UPDATE user_session_devices SET isActive = 0 WHERE id = :deviceId")
    suspend fun revokeDevice(deviceId: Long)
}

@Dao
interface EquipmentFaultDao {
    @Query("SELECT * FROM equipment_faults ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<EquipmentFaultEntity>>

    @Query("SELECT * FROM equipment_faults WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EquipmentFaultEntity?

    @Query("SELECT * FROM equipment_faults ORDER BY id ASC")
    suspend fun getAll(): List<EquipmentFaultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fault: EquipmentFaultEntity): Long

    @Update
    suspend fun update(fault: EquipmentFaultEntity)
}

@Dao
interface SyncOutboxDao {
    @Query("SELECT * FROM sync_outbox ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<SyncOutboxEntity>>

    @Query("SELECT * FROM sync_outbox WHERE status IN ('PENDING', 'ERROR') ORDER BY createdAt ASC")
    suspend fun getPendingOrError(): List<SyncOutboxEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncOutboxEntity): Long

    @Query("UPDATE sync_outbox SET status = :status, lastError = :lastError, retryCount = :retryCount WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, lastError: String = "", retryCount: Int = 0)

    @Query("DELETE FROM sync_outbox WHERE status = 'SYNCED'")
    suspend fun clearSynced()
}

@Dao
interface CloudSyncMapDao {
    @Query("SELECT * FROM cloud_sync_map WHERE entityType = :entityType AND localId = :localId LIMIT 1")
    suspend fun getByLocal(entityType: String, localId: Long): com.example.data.local.entity.CloudSyncMapEntity?

    @Query("SELECT * FROM cloud_sync_map WHERE companyId = :companyId AND entityType = :entityType AND cloudId = :cloudId LIMIT 1")
    suspend fun getByCloud(companyId: String, entityType: String, cloudId: String): com.example.data.local.entity.CloudSyncMapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: com.example.data.local.entity.CloudSyncMapEntity)

    @Query("DELETE FROM cloud_sync_map WHERE entityType = :entityType AND localId = :localId")
    suspend fun deleteByLocal(entityType: String, localId: Long)

    @Query("DELETE FROM cloud_sync_map WHERE companyId != :companyId")
    suspend fun clearOtherCompanies(companyId: String)
}
