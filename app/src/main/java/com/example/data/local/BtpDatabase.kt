package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ChatChannelDao
import com.example.data.local.dao.CloudSyncMapDao
import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.ClientDao
import com.example.data.local.dao.EmployeeDao
import com.example.data.local.dao.EquipmentDao
import com.example.data.local.dao.EquipmentFaultDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.InvoiceDao
import com.example.data.local.dao.MaterialDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.NotificationPreferenceDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.ScheduleTaskDao
import com.example.data.local.dao.SiteDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.dao.SyncOutboxDao
import com.example.data.local.dao.UserAccountDao
import com.example.data.local.dao.UserSessionDeviceDao
import com.example.data.local.entity.ChatChannelEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.CloudSyncMapEntity
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

@Database(
    entities = [
        UserAccountEntity::class,
        ClientEntity::class,
        SupplierEntity::class,
        ProjectEntity::class,
        SiteEntity::class,
        EmployeeEntity::class,
        EquipmentEntity::class,
        MaterialEntity::class,
        ExpenseEntity::class,
        InvoiceEntity::class,
        ScheduleTaskEntity::class,
        ChatChannelEntity::class,
        ChatMessageEntity::class,
        NotificationEntity::class,
        NotificationPreferenceEntity::class,
        UserSessionDeviceEntity::class,
        EquipmentFaultEntity::class,
        SyncOutboxEntity::class,
        CloudSyncMapEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class BtpDatabase : RoomDatabase() {
    abstract fun userAccountDao(): UserAccountDao
    abstract fun clientDao(): ClientDao
    abstract fun supplierDao(): SupplierDao
    abstract fun projectDao(): ProjectDao
    abstract fun siteDao(): SiteDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun materialDao(): MaterialDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun scheduleTaskDao(): ScheduleTaskDao
    abstract fun chatChannelDao(): ChatChannelDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun notificationPreferenceDao(): NotificationPreferenceDao
    abstract fun userSessionDeviceDao(): UserSessionDeviceDao
    abstract fun equipmentFaultDao(): EquipmentFaultDao
    abstract fun syncOutboxDao(): SyncOutboxDao
    abstract fun cloudSyncMapDao(): CloudSyncMapDao

    companion object {
        @Volatile
        private var INSTANCE: BtpDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_accounts ADD COLUMN preferredLanguageCode TEXT NOT NULL DEFAULT 'ar'")
                db.execSQL("ALTER TABLE user_accounts ADD COLUMN branchName TEXT NOT NULL DEFAULT 'المقر الرئيسي'")
                db.execSQL("ALTER TABLE user_accounts ADD COLUMN avatarUri TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_accounts ADD COLUMN rewardPoints INTEGER NOT NULL DEFAULT 120")

                db.execSQL("ALTER TABLE schedule_tasks ADD COLUMN assignedToUserId INTEGER")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS chat_channels (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        channelType TEXT NOT NULL,
                        projectName TEXT NOT NULL,
                        participantIdsCsv TEXT NOT NULL,
                        lastMessagePreview TEXT NOT NULL,
                        lastMessageAt INTEGER NOT NULL,
                        unreadCount INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS chat_messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        channelId INTEGER NOT NULL,
                        senderUserId INTEGER NOT NULL,
                        senderName TEXT NOT NULL,
                        senderRole TEXT NOT NULL,
                        content TEXT NOT NULL,
                        attachmentType TEXT NOT NULL,
                        attachmentName TEXT NOT NULL,
                        attachmentUri TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        isRead INTEGER NOT NULL,
                        syncStatus TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notifications (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        category TEXT NOT NULL,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        targetTab TEXT NOT NULL,
                        targetSubTab INTEGER NOT NULL,
                        relatedEntityId INTEGER,
                        isRead INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        audience TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notification_preferences (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        enabled INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_session_devices (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        deviceName TEXT NOT NULL,
                        deviceModel TEXT NOT NULL,
                        locationLabel TEXT NOT NULL,
                        lastActiveAt INTEGER NOT NULL,
                        isCurrentDevice INTEGER NOT NULL,
                        isActive INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS equipment_faults (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        equipmentName TEXT NOT NULL,
                        siteName TEXT NOT NULL,
                        severity TEXT NOT NULL,
                        description TEXT NOT NULL,
                        reportedByUserId INTEGER NOT NULL,
                        reportedByName TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        status TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_outbox (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entityType TEXT NOT NULL,
                        entityId INTEGER NOT NULL,
                        operation TEXT NOT NULL,
                        payloadSummary TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        retryCount INTEGER NOT NULL,
                        lastError TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cloud_sync_map (
                        entityType TEXT NOT NULL,
                        localId INTEGER NOT NULL,
                        cloudId TEXT NOT NULL,
                        companyId TEXT NOT NULL,
                        syncedRemote INTEGER NOT NULL,
                        lastSyncedAt INTEGER NOT NULL,
                        PRIMARY KEY(entityType, localId)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): BtpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BtpDatabase::class.java,
                    "btp_manager_offline.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
