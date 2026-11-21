package com.example

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.core.localization.AppLanguage
import com.example.core.localization.stringsFor
import com.example.core.security.RolePermissionPolicy
import com.example.core.security.SecurityUtils
import com.example.data.local.BtpDatabase
import com.example.data.local.entity.ProjectEntity
import com.example.data.repository.AuthResult
import com.example.data.repository.BtpRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches BTP Manager`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BTP Manager", appName)
    }

    @Test
    fun `password hashing verifies correctly`() {
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword("123456", salt)
        assertTrue(SecurityUtils.verifyPassword("123456", salt, hash))
        assertFalse(SecurityUtils.verifyPassword("wrong", salt, hash))
    }

    @Test
    fun `offline room database seeds and authenticates by phone and username`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BtpDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = BtpRepository(db)
            repo.initializeOfflineData()

            val adminByPhone = repo.authenticate("0550 00 00 01", "123456")
            assertTrue(adminByPhone is AuthResult.Success)
            assertEquals("مدير عام", (adminByPhone as AuthResult.Success).user.role)

            val engineerByPhone = repo.authenticate("0550000002", "123456")
            assertTrue(engineerByPhone is AuthResult.Success)

            val invalid = repo.authenticate("0550000001", "000000")
            assertTrue(invalid is AuthResult.Error)

            val initialProjects = repo.projects.first()
            assertTrue(initialProjects.isNotEmpty())

            repo.saveProject(
                ProjectEntity(
                    code = "PRJ-TEST",
                    name = "مشروع تجريبي",
                    clientName = "عميل",
                    location = "الجزائر",
                    budget = 500000.0,
                    startDate = "2026-11-19",
                    endDate = "2027-01-01"
                )
            )
            val updatedProjects = repo.projects.first()
            assertEquals(initialProjects.size + 1, updatedProjects.size)
        } finally {
            db.close()
        }
    }

    @Test
    fun `all 9 requested languages plus existing load with non-blank topbar strings and RTL direction`() {
        val requiredCodes = listOf("ar", "fr", "en", "es", "de", "it", "pt", "tr", "zh")
        requiredCodes.forEach { code ->
            val lang = AppLanguage.fromCode(code)
            assertEquals(code, lang.code)
            val strings = stringsFor(lang)
            assertTrue(strings.loginButton.isNotBlank())
            assertTrue(strings.projectsHubTitle.isNotBlank())
            assertTrue(strings.translateTerm("قيد التنفيذ").isNotBlank())
            assertTrue(strings.authErrorText("ERR_INVALID_CREDENTIALS").isNotBlank())
            assertTrue(strings.topBar.chatTitle.isNotBlank())
            assertTrue(strings.topBar.notificationsTitle.isNotBlank())
            assertTrue(strings.topBar.syncStatusTitle.isNotBlank())
            assertTrue(strings.topBar.tasksCenterTitle.isNotBlank())
            assertTrue(strings.topBar.quickActionsTitle.isNotBlank())
            assertTrue(strings.topBar.globalSearchTitle.isNotBlank())
        }
        assertTrue(AppLanguage.AR.isRtl)
        assertFalse(AppLanguage.FR.isRtl)
        assertFalse(AppLanguage.EN.isRtl)
        assertFalse(AppLanguage.PT.isRtl)
        assertFalse(AppLanguage.ZH.isRtl)
    }

    @Test
    fun `per-user language preference persists independently without affecting other users`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BtpDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = BtpRepository(db)
            repo.initializeOfflineData()

            val admin = (repo.authenticate("admin", "123456") as AuthResult.Success).user
            val engineer = (repo.authenticate("engineer", "123456") as AuthResult.Success).user

            repo.updateUserLanguage(admin.id, "es")
            repo.updateUserLanguage(engineer.id, "pt")

            val reloadedAdmin = repo.getUserById(admin.id)!!
            val reloadedEngineer = repo.getUserById(engineer.id)!!

            assertEquals("es", reloadedAdmin.preferredLanguageCode)
            assertEquals("pt", reloadedEngineer.preferredLanguageCode)
        } finally {
            db.close()
        }
    }

    @Test
    fun `btp chat sends messages with attachments and queues offline sync outbox`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BtpDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = BtpRepository(db)
            repo.initializeOfflineData()

            val engineer = (repo.authenticate("engineer", "123456") as AuthResult.Success).user
            val channels = repo.chatChannels.first()
            assertTrue(channels.isNotEmpty())
            val channel = channels.first()

            repo.sendChatMessage(
                channelId = channel.id,
                sender = engineer,
                content = "تم رفع تقرير الفحص",
                attachmentType = "DOCUMENT",
                attachmentName = "Inspection_SiteB.pdf",
                attachmentUri = "content://docs/inspection.pdf"
            )

            val msgs = repo.observeChannelMessages(channel.id).first()
            assertTrue(msgs.any { it.content == "تم رفع تقرير الفحص" && it.attachmentName == "Inspection_SiteB.pdf" })

            repo.markChannelRead(channel.id)
            val updatedChannel = repo.chatChannels.first().first { it.id == channel.id }
            assertEquals(0, updatedChannel.unreadCount)

            val outbox = repo.syncOutbox.first()
            assertTrue(outbox.any { it.entityType == "CHAT_MESSAGE" && it.operation == "MESSAGE" && it.status == "PENDING" })
        } finally {
            db.close()
        }
    }

    @Test
    fun `role permissions isolate finance and admin security and persist notification preferences`() = runTest {
        val adminPerms = RolePermissionPolicy.permissionsFor("مدير عام")
        val engPerms = RolePermissionPolicy.permissionsFor("مهندس موقع")

        assertTrue(adminPerms.canViewFinance)
        assertTrue(adminPerms.canPostAnnouncement)
        assertTrue("FINANCE" in adminPerms.allowedNotificationCategories)
        assertTrue("ADMIN_SECURITY" in adminPerms.allowedNotificationCategories)

        assertFalse(engPerms.canViewFinance)
        assertFalse(engPerms.canAddExpense)
        assertFalse(engPerms.canPostAnnouncement)
        assertFalse("FINANCE" in engPerms.allowedNotificationCategories)
        assertFalse("ADMIN_SECURITY" in engPerms.allowedNotificationCategories)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BtpDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = BtpRepository(db)
            repo.initializeOfflineData()
            val engineer = (repo.authenticate("engineer", "123456") as AuthResult.Success).user

            repo.setNotificationPreference(engineer.id, "EQUIPMENT", false)
            val prefs = repo.observeNotificationPreferences(engineer.id).first()
            assertEquals(false, prefs.first { it.category == "EQUIPMENT" }.enabled)
        } finally {
            db.close()
        }
    }

    @Test
    fun `equipment fault report updates status creates notification and device session revoke works`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BtpDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = BtpRepository(db)
            repo.initializeOfflineData()
            val engineer = (repo.authenticate("engineer", "123456") as AuthResult.Success).user

            repo.reportEquipmentFault(
                equipmentName = "حفارة هيدروليكية CAT 320",
                siteName = "ورشة المقطع الكيلومتري 14",
                severity = "عالية",
                description = "تسرب زيت هيدروليك من الذراع الرئيسي",
                reporter = engineer
            )

            val equipment = repo.equipment.first().first { it.name == "حفارة هيدروليكية CAT 320" }
            assertEquals("صيانة", equipment.status)

            val notifications = repo.notifications.first()
            assertTrue(notifications.any { it.category == "EQUIPMENT" && it.message.contains("تسرب زيت") })

            repo.registerCurrentDeviceSession(engineer.id, "Pixel 9 Pro", "Android 15")
            val devices = repo.observeUserDevices(engineer.id).first()
            assertTrue(devices.size >= 2)
            val secondary = devices.first { !it.isCurrentDevice }
            repo.revokeDeviceSession(secondary.id)
            val updatedDevices = repo.observeUserDevices(engineer.id).first()
            assertFalse(updatedDevices.first { it.id == secondary.id }.isActive)
        } finally {
            db.close()
        }
    }

    @Test
    fun `non destructive migration 2 to 3 preserves existing data and creates top bar tables`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migration_test_btp.db"
        context.deleteDatabase(dbName)

        // Create a v2 schema database manually and insert sample records
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS user_accounts (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                fullName TEXT NOT NULL,
                                username TEXT NOT NULL,
                                phone TEXT NOT NULL,
                                role TEXT NOT NULL,
                                passwordHash TEXT NOT NULL,
                                salt TEXT NOT NULL,
                                isActive INTEGER NOT NULL,
                                createdAt INTEGER NOT NULL,
                                lastLoginAt INTEGER
                            )
                            """.trimIndent()
                        )
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS schedule_tasks (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                title TEXT NOT NULL,
                                projectName TEXT NOT NULL,
                                siteName TEXT NOT NULL,
                                assignedTeam TEXT NOT NULL,
                                startDate TEXT NOT NULL,
                                dueDate TEXT NOT NULL,
                                priority TEXT NOT NULL,
                                isCompleted INTEGER NOT NULL,
                                progress INTEGER NOT NULL
                            )
                            """.trimIndent()
                        )
                        db.execSQL(
                            "INSERT INTO user_accounts (fullName, username, phone, role, passwordHash, salt, isActive, createdAt) VALUES ('Legacy User', 'legacy', '0550998877', 'مدير عام', 'hash', 'salt', 1, 1000)"
                        )
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )
        val rawDb = helper.writableDatabase
        BtpDatabase.MIGRATION_2_3.migrate(rawDb)

        val cursor = rawDb.query("SELECT fullName, preferredLanguageCode, branchName, rewardPoints FROM user_accounts WHERE username = 'legacy'")
        assertTrue(cursor.moveToFirst())
        assertEquals("Legacy User", cursor.getString(0))
        assertEquals("ar", cursor.getString(1))
        assertNotNull(cursor.getString(2))
        assertEquals(120, cursor.getInt(3))
        cursor.close()

        val chatCursor = rawDb.query("SELECT COUNT(*) FROM chat_channels")
        assertTrue(chatCursor.moveToFirst())
        assertEquals(0, chatCursor.getInt(0))
        chatCursor.close()

        rawDb.close()
        helper.close()
        context.deleteDatabase(dbName)
    }
}
