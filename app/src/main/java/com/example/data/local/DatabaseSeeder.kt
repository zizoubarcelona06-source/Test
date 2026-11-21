package com.example.data.local

import com.example.core.security.SecurityUtils
import com.example.data.local.entity.ChatChannelEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.EquipmentEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.MaterialEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScheduleTaskEntity
import com.example.data.local.entity.SiteEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.UserAccountEntity

object DatabaseSeeder {

    suspend fun ensureUsersSeeded(db: BtpDatabase) {
        if (db.userAccountDao().count() == 0) {
            val adminSalt = SecurityUtils.generateSalt()
            val adminHash = SecurityUtils.hashPassword("123456", adminSalt)
            db.userAccountDao().insert(
                UserAccountEntity(
                    fullName = "أحمد بن علي",
                    username = "admin",
                    phone = "0550000001",
                    role = "مدير عام",
                    passwordHash = adminHash,
                    salt = adminSalt,
                    isActive = true,
                    preferredLanguageCode = "ar",
                    branchName = "المقر الرئيسي - الجزائر",
                    rewardPoints = 240
                )
            )

            val engSalt = SecurityUtils.generateSalt()
            val engHash = SecurityUtils.hashPassword("123456", engSalt)
            db.userAccountDao().insert(
                UserAccountEntity(
                    fullName = "كريم منصوري",
                    username = "engineer",
                    phone = "0550000002",
                    role = "مهندس موقع",
                    passwordHash = engHash,
                    salt = engSalt,
                    isActive = true,
                    preferredLanguageCode = "ar",
                    branchName = "ورشة علي منجلي",
                    rewardPoints = 155
                )
            )

            val accSalt = SecurityUtils.generateSalt()
            val accHash = SecurityUtils.hashPassword("123456", accSalt)
            db.userAccountDao().insert(
                UserAccountEntity(
                    fullName = "سهام بوعلام",
                    username = "accountant",
                    phone = "0550000003",
                    role = "محاسب مالي",
                    passwordHash = accHash,
                    salt = accSalt,
                    isActive = true,
                    preferredLanguageCode = "fr",
                    branchName = "الإدارة المالية",
                    rewardPoints = 190
                )
            )
        }
    }

    suspend fun ensureInitialDataSeeded(db: BtpDatabase) {
        ensureUsersSeeded(db)
        if (db.projectDao().count() == 0) {
            seedCompanySampleData(db)
        }
        ensureTopBarSeedData(db)
    }

    suspend fun resetAndSeedSampleData(db: BtpDatabase) {
        db.clientDao().deleteAll()
        db.supplierDao().deleteAll()
        db.projectDao().deleteAll()
        db.siteDao().deleteAll()
        db.employeeDao().deleteAll()
        db.equipmentDao().deleteAll()
        db.materialDao().deleteAll()
        db.expenseDao().deleteAll()
        db.invoiceDao().deleteAll()
        db.scheduleTaskDao().deleteAll()
        seedCompanySampleData(db)
        ensureTopBarSeedData(db)
    }

    suspend fun ensureTopBarSeedData(db: BtpDatabase) {
        val now = System.currentTimeMillis()
        if (db.chatChannelDao().count() == 0) {
            val chAnnouncements = db.chatChannelDao().insert(
                ChatChannelEntity(
                    name = "إعلانات وتعليمات الإدارة العليا",
                    channelType = "ANNOUNCEMENT",
                    projectName = "جميع الورشات",
                    participantIdsCsv = "1,2,3",
                    lastMessagePreview = "التزام ارتداء الخوذات والأحذية الواقية إلزامي في جميع المواقع.",
                    lastMessageAt = now - 1_800_000L,
                    unreadCount = 1
                )
            )
            db.chatMessageDao().insert(
                ChatMessageEntity(
                    channelId = chAnnouncements,
                    senderUserId = 1L,
                    senderName = "أحمد بن علي",
                    senderRole = "مدير عام",
                    content = "تذكير لجميع رؤساء الورشات بتحديث التقارير اليومية قبل الساعة 17:00.",
                    timestamp = now - 7_200_000L,
                    isRead = true,
                    syncStatus = "SYNCED"
                )
            )
            db.chatMessageDao().insert(
                ChatMessageEntity(
                    channelId = chAnnouncements,
                    senderUserId = 1L,
                    senderName = "أحمد بن علي",
                    senderRole = "مدير عام",
                    content = "التزام ارتداء الخوذات والأحذية الواقية إلزامي في جميع المواقع.",
                    timestamp = now - 1_800_000L,
                    isRead = false,
                    syncStatus = "SYNCED"
                )
            )

            val chProject1 = db.chatChannelDao().insert(
                ChatChannelEntity(
                    name = "مشروع إقامة الياسمين السكنية",
                    channelType = "PROJECT",
                    projectName = "إقامة الياسمين السكنية (120 وحدة)",
                    participantIdsCsv = "1,2",
                    lastMessagePreview = "تم إتمام تسليح أعمدة الطابق السادس وجاهز للصب.",
                    lastMessageAt = now - 3_600_000L,
                    unreadCount = 2
                )
            )
            db.chatMessageDao().insert(
                ChatMessageEntity(
                    channelId = chProject1,
                    senderUserId = 2L,
                    senderName = "كريم منصوري",
                    senderRole = "مهندس موقع",
                    content = "صباح الخير، فريق الخرسانة باشر فحص القوالب في العمارة B.",
                    timestamp = now - 5_400_000L,
                    isRead = false,
                    syncStatus = "SYNCED"
                )
            )
            db.chatMessageDao().insert(
                ChatMessageEntity(
                    channelId = chProject1,
                    senderUserId = 2L,
                    senderName = "كريم منصوري",
                    senderRole = "مهندس موقع",
                    content = "تم إتمام تسليح أعمدة الطابق السادس وجاهز للصب.",
                    attachmentType = "DOCUMENT",
                    attachmentName = "PV_Ferraillage_BlocB.pdf",
                    timestamp = now - 3_600_000L,
                    isRead = false,
                    syncStatus = "SYNCED"
                )
            )

            val chSiteRoad = db.chatChannelDao().insert(
                ChatChannelEntity(
                    name = "ورشة الطريق السريع كم 14",
                    channelType = "SITE",
                    projectName = "توسعة الطريق السريع المقطع الشرقي",
                    participantIdsCsv = "1,2",
                    lastMessagePreview = "الشاحنة القلابة EQ-TRK-04 تحتاج فحص مكابح.",
                    lastMessageAt = now - 9_000_000L,
                    unreadCount = 1
                )
            )
            db.chatMessageDao().insert(
                ChatMessageEntity(
                    channelId = chSiteRoad,
                    senderUserId = 2L,
                    senderName = "كريم منصوري",
                    senderRole = "مهندس موقع",
                    content = "الشاحنة القلابة EQ-TRK-04 تحتاج فحص مكابح.",
                    timestamp = now - 9_000_000L,
                    isRead = false,
                    syncStatus = "SYNCED"
                )
            )

            val chDirect = db.chatChannelDao().insert(
                ChatChannelEntity(
                    name = "كريم منصوري ↔ أحمد بن علي",
                    channelType = "DIRECT",
                    projectName = "تواصل مباشر",
                    participantIdsCsv = "1,2",
                    lastMessagePreview = "نحتاج تأكيد طلبية الإسمنت CPJ 42.5 لورشة علي منجلي.",
                    lastMessageAt = now - 12_000_000L,
                    unreadCount = 1
                )
            )
            db.chatMessageDao().insert(
                ChatMessageEntity(
                    channelId = chDirect,
                    senderUserId = 2L,
                    senderName = "كريم منصوري",
                    senderRole = "مهندس موقع",
                    content = "نحتاج تأكيد طلبية الإسمنت CPJ 42.5 لورشة علي منجلي.",
                    timestamp = now - 12_000_000L,
                    isRead = false,
                    syncStatus = "SYNCED"
                )
            )
        }

        if (db.notificationDao().count() == 0) {
            val seededNotifications = listOf(
                NotificationEntity(
                    category = "CHAT",
                    title = "رسالة جديدة في مشروع إقامة الياسمين",
                    message = "كريم منصوري أرفق محضر PV_Ferraillage_BlocB.pdf",
                    targetTab = "DASHBOARD",
                    isRead = false,
                    createdAt = now - 1_200_000L,
                    audience = "ALL"
                ),
                NotificationEntity(
                    category = "ATTENDANCE",
                    title = "تسجيل الحضور اليومي للورشات",
                    message = "4 عمال حاضرون و1 في إجازة (مصطفى لعروسي)",
                    targetTab = "RESOURCES",
                    targetSubTab = 0,
                    isRead = false,
                    createdAt = now - 2_400_000L,
                    audience = "ALL"
                ),
                NotificationEntity(
                    category = "LEAVE",
                    title = "إجازة مصادق عليها",
                    message = "مصطفى لعروسي (كهربائي) في إجازة سنوية حتى نهاية الأسبوع",
                    targetTab = "RESOURCES",
                    targetSubTab = 0,
                    isRead = true,
                    createdAt = now - 3_600_000L,
                    audience = "ALL"
                ),
                NotificationEntity(
                    category = "REWARDS",
                    title = "مكافأة أداء ميداني +25 نقطة",
                    message = "إنجاز صب بلاطة الطابق الخامس قبل الموعد بدون حوادث سلامة",
                    targetTab = "DASHBOARD",
                    isRead = false,
                    createdAt = now - 4_800_000L,
                    audience = "ALL"
                ),
                NotificationEntity(
                    category = "EQUIPMENT",
                    title = "تنبيه صيانة معدة",
                    message = "الشاحنة القلابة Mercedes Actros (EQ-TRK-04) في وضع الصيانة الدورية",
                    targetTab = "RESOURCES",
                    targetSubTab = 1,
                    isRead = false,
                    createdAt = now - 5_600_000L,
                    audience = "ALL"
                ),
                NotificationEntity(
                    category = "INVENTORY",
                    title = "انخفاض مخزون الإسمنت والطوب",
                    message = "إسمنت CPJ 42.5 (18 طن) والطوب الأحمر (2400) دون الحد الأدنى",
                    targetTab = "RESOURCES",
                    targetSubTab = 2,
                    isRead = false,
                    createdAt = now - 6_400_000L,
                    audience = "ALL"
                ),
                NotificationEntity(
                    category = "APPROVALS",
                    title = "بانتظار الاعتماد الإداري",
                    message = "طلب توريد 30 طن إسمنت لورشة العمارة A و B بانتظار التوقيع",
                    targetTab = "PROJECTS",
                    targetSubTab = 0,
                    isRead = false,
                    createdAt = now - 7_200_000L,
                    audience = "ALL"
                ),
                NotificationEntity(
                    category = "PROJECTS",
                    title = "تقدم مشروع المركز التجاري النخيل",
                    message = "بلغت نسبة الإنجاز 92% مع انطلاق اختبارات المصاعد والواجهات",
                    targetTab = "PROJECTS",
                    targetSubTab = 0,
                    isRead = true,
                    createdAt = now - 8_000_000L,
                    audience = "ALL"
                ),
                NotificationEntity(
                    category = "FINANCE",
                    title = "فاتورة عميل متأخرة السداد",
                    message = "الفاتورة INV-2026-112 بقيمة 3,100,000 د.ج مستحقة من شركة الأمل",
                    targetTab = "FINANCE",
                    targetSubTab = 1,
                    isRead = false,
                    createdAt = now - 9_000_000L,
                    audience = "FINANCE"
                ),
                NotificationEntity(
                    category = "TASKS",
                    title = "مهمة متأخرة تحتاج متابعة",
                    message = "مراجعة مخططات الأساسات والتصريف الصحي لمشروع الطريق السريع",
                    targetTab = "PROJECTS",
                    targetSubTab = 2,
                    isRead = false,
                    createdAt = now - 10_000_000L,
                    audience = "ALL"
                ),
                NotificationEntity(
                    category = "ADMIN_SECURITY",
                    title = "تحديث نظام الصلاحيات والمزامنة",
                    message = "تم تفعيل التحقق من الجلسات النشطة لكل مستخدم وعزل صلاحيات المالية",
                    targetTab = "REPORTS",
                    targetSubTab = 1,
                    isRead = false,
                    createdAt = now - 11_000_000L,
                    audience = "ADMIN"
                )
            )
            seededNotifications.forEach { db.notificationDao().insert(it) }
        }
    }

    private suspend fun seedCompanySampleData(db: BtpDatabase) {
        // Clients
        db.clientDao().insert(
            ClientEntity(
                name = "ديوان الترقية والتسيير العقاري",
                companyName = "OPGI - ولاية الوسط",
                phone = "0550 12 34 56",
                email = "contact@opgi-centre.dz",
                address = "شارع الاستقلال، الجزائر",
                taxId = "099816004512345",
                notes = "عميل حكومي - مشاريع سكنية"
            )
        )
        db.clientDao().insert(
            ClientEntity(
                name = "شركة الأمل للتطوير العقاري",
                companyName = "الأمل العقارية ش.ذ.م.م",
                phone = "0661 88 45 20",
                email = "info@alamal-immo.com",
                address = "حي العقيد لطفي، وهران",
                taxId = "001531008941203",
                notes = "مجمعات تجارية وسكنية"
            )
        )
        db.clientDao().insert(
            ClientEntity(
                name = "مديرية الأشغال العمومية",
                companyName = "DTP - القطاع الشرقي",
                phone = "0552 44 77 10",
                email = "marches@dtp-est.dz",
                address = "المنطقة الإدارية، قسنطينة",
                taxId = "099425003119800",
                notes = "طرق وجسور"
            )
        )

        // Suppliers
        db.supplierDao().insert(
            SupplierEntity(
                name = "مصنع إسمنت الشرق",
                category = "خرسانة وإسمنت",
                phone = "0770 33 21 90",
                email = "ventes@ciment-est.dz",
                address = "المنطقة الصناعية، سطيف",
                balanceOwed = 480000.0,
                notes = "توريد يومي للإسمنت المقاوم"
            )
        )
        db.supplierDao().insert(
            SupplierEntity(
                name = "مجمع الحديد المتحد",
                category = "حديد وصلب",
                phone = "0555 91 02 44",
                email = "orders@steel-united.com",
                address = "ميناء عنابة التجاري",
                balanceOwed = 820000.0,
                notes = "حديد تسليح عالي الصلابة"
            )
        )
        db.supplierDao().insert(
            SupplierEntity(
                name = "محاجر الأطلس للركام",
                category = "خرسانة وإسمنت",
                phone = "0662 19 80 33",
                email = "atlas.aggregates@mail.com",
                address = "الطريق الوطني رقم 5، البويرة",
                balanceOwed = 150000.0,
                notes = "حصى ورمل بناء مغسول"
            )
        )

        // Projects
        val p1 = db.projectDao().insert(
            ProjectEntity(
                code = "PRJ-2026-01",
                name = "إقامة الياسمين السكنية (120 وحدة)",
                clientName = "ديوان الترقية والتسيير العقاري",
                location = "المدينة الجديدة، علي منجلي",
                budget = 28500000.0,
                spentAmount = 19200000.0,
                startDate = "2026-01-15",
                endDate = "2027-04-30",
                progressPercent = 68,
                status = "قيد التنفيذ",
                description = "بناء 4 عمارات سكنية مع المرافق الخارجية وشبكات الصرف."
            )
        )
        val p2 = db.projectDao().insert(
            ProjectEntity(
                code = "PRJ-2026-02",
                name = "توسعة الطريق السريع المقطع الشرقي",
                clientName = "مديرية الأشغال العمومية",
                location = "محور قسنطينة - سطيف",
                budget = 42000000.0,
                spentAmount = 18500000.0,
                startDate = "2026-03-01",
                endDate = "2027-09-15",
                progressPercent = 44,
                status = "قيد التنفيذ",
                description = "أشغال التسطيح، وضع الأسفلت، وبناء منشآت تصريف المياه."
            )
        )
        val p3 = db.projectDao().insert(
            ProjectEntity(
                code = "PRJ-2026-03",
                name = "المركز التجاري النخيل",
                clientName = "شركة الأمل للتطوير العقاري",
                location = "حي العقيد لطفي، وهران",
                budget = 16000000.0,
                spentAmount = 14800000.0,
                startDate = "2025-07-10",
                endDate = "2026-12-20",
                progressPercent = 92,
                status = "قيد التنفيذ",
                description = "التشطيبات النهائية والواجهات الزجاجية والتكييف المركزي."
            )
        )
        db.projectDao().insert(
            ProjectEntity(
                code = "PRJ-2026-04",
                name = "بناء ثانوية المستقبل النموذجية",
                clientName = "ديوان الترقية والتسيير العقاري",
                location = "الجزائر الوسطى",
                budget = 12500000.0,
                spentAmount = 1200000.0,
                startDate = "2026-12-01",
                endDate = "2027-11-30",
                progressPercent = 10,
                status = "مخطط",
                description = "تهيئة الأرضية والحفر الأولي للأساسات."
            )
        )

        // Sites
        db.siteDao().insert(
            SiteEntity(
                projectId = p1,
                projectName = "إقامة الياسمين السكنية (120 وحدة)",
                name = "ورشة العمارة A و B",
                address = "القطاع 4 - علي منجلي",
                supervisorName = "كريم منصوري",
                workerCount = 34,
                safetyStatus = "آمن",
                status = "نشط",
                dailyNote = "تم صب بلاطة الطابق الخامس بنجاح."
            )
        )
        db.siteDao().insert(
            SiteEntity(
                projectId = p2,
                projectName = "توسعة الطريق السريع المقطع الشرقي",
                name = "ورشة المقطع الكيلومتري 14",
                address = "الطريق الوطني 5 - كم 14",
                supervisorName = "رشيد بلقاسم",
                workerCount = 26,
                safetyStatus = "تنبيه",
                status = "نشط",
                dailyNote = "تنبيه: الحاجة إلى إشارات مرور إضافية ليلاً."
            )
        )
        db.siteDao().insert(
            SiteEntity(
                projectId = p3,
                projectName = "المركز التجاري النخيل",
                name = "ورشة التشطيبات والواجهات",
                address = "حي العقيد لطفي - وهران",
                supervisorName = "سمير بوزيد",
                workerCount = 19,
                safetyStatus = "آمن",
                status = "نشط",
                dailyNote = "تركيب المصاعد الكهربائية جاري وفق الجدول."
            )
        )

        // Employees
        db.employeeDao().insert(
            EmployeeEntity(
                fullName = "كريم منصوري",
                role = "مهندس مدني",
                phone = "0551 22 44 88",
                nationalId = "198904120031",
                dailyWage = 6500.0,
                assignedSiteName = "ورشة العمارة A و B",
                attendanceStatus = "حاضر",
                hireDate = "2023-05-10"
            )
        )
        db.employeeDao().insert(
            EmployeeEntity(
                fullName = "رشيد بلقاسم",
                role = "مراقب ورشة",
                phone = "0663 11 78 90",
                nationalId = "198511030098",
                dailyWage = 4800.0,
                assignedSiteName = "ورشة المقطع الكيلومتري 14",
                attendanceStatus = "حاضر",
                hireDate = "2022-02-14"
            )
        )
        db.employeeDao().insert(
            EmployeeEntity(
                fullName = "يوسف زروقي",
                role = "سائق آليات",
                phone = "0771 54 32 10",
                nationalId = "199108210045",
                dailyWage = 4200.0,
                assignedSiteName = "ورشة المقطع الكيلومتري 14",
                attendanceStatus = "حاضر",
                hireDate = "2024-01-20"
            )
        )
        db.employeeDao().insert(
            EmployeeEntity(
                fullName = "عبد القادر حداد",
                role = "رئيس بناء",
                phone = "0558 90 12 34",
                nationalId = "198207150012",
                dailyWage = 4500.0,
                assignedSiteName = "ورشة العمارة A و B",
                attendanceStatus = "حاضر",
                hireDate = "2021-09-01"
            )
        )
        db.employeeDao().insert(
            EmployeeEntity(
                fullName = "مصطفى لعروسي",
                role = "كهربائي",
                phone = "0669 45 67 89",
                nationalId = "199403190077",
                dailyWage = 3800.0,
                assignedSiteName = "ورشة التشطيبات والواجهات",
                attendanceStatus = "إجازة",
                hireDate = "2024-06-11"
            )
        )

        // Equipment
        db.equipmentDao().insert(
            EquipmentEntity(
                name = "حفارة هيدروليكية CAT 320",
                code = "EQ-EXC-01",
                category = "حفارة",
                status = "في الخدمة",
                assignedSiteName = "ورشة المقطع الكيلومتري 14",
                nextMaintenanceDate = "2026-12-10",
                hourlyCost = 4500.0
            )
        )
        db.equipmentDao().insert(
            EquipmentEntity(
                name = "رافعة برجية Liebherr 150",
                code = "EQ-CRN-02",
                category = "رافعة",
                status = "في الخدمة",
                assignedSiteName = "ورشة العمارة A و B",
                nextMaintenanceDate = "2026-12-25",
                hourlyCost = 6200.0
            )
        )
        db.equipmentDao().insert(
            EquipmentEntity(
                name = "شاحنة قلابة Mercedes Actros",
                code = "EQ-TRK-04",
                category = "شاحنة",
                status = "صيانة",
                assignedSiteName = "ورشة المقطع الكيلومتري 14",
                nextMaintenanceDate = "2026-11-22",
                hourlyCost = 3200.0
            )
        )
        db.equipmentDao().insert(
            EquipmentEntity(
                name = "خلاطة خرسانة مركزية 500L",
                code = "EQ-MIX-03",
                category = "خلاطة",
                status = "متاح",
                assignedSiteName = "المستودع المركزي",
                nextMaintenanceDate = "2027-01-15",
                hourlyCost = 1800.0
            )
        )

        // Materials (2 low stock items for visible alerts)
        db.materialDao().insert(
            MaterialEntity(
                name = "إسمنت مقاوم CPJ 42.5",
                sku = "MAT-CEM-42",
                unit = "طن",
                quantityInStock = 18.0,
                minThreshold = 25.0,
                unitPrice = 14500.0,
                supplierName = "مصنع إسمنت الشرق",
                warehouseLocation = "مخزن أ - علي منجلي"
            )
        )
        db.materialDao().insert(
            MaterialEntity(
                name = "حديد تسليح 12 مم",
                sku = "MAT-STL-12",
                unit = "طن",
                quantityInStock = 42.0,
                minThreshold = 15.0,
                unitPrice = 112000.0,
                supplierName = "مجمع الحديد المتحد",
                warehouseLocation = "ساحة الحديد الرئيسية"
            )
        )
        db.materialDao().insert(
            MaterialEntity(
                name = "رمل بناء مغسول",
                sku = "MAT-SND-01",
                unit = "م³",
                quantityInStock = 85.0,
                minThreshold = 30.0,
                unitPrice = 2400.0,
                supplierName = "محاجر الأطلس للركام",
                warehouseLocation = "مخزن الركام 2"
            )
        )
        db.materialDao().insert(
            MaterialEntity(
                name = "طوب أحمر 12 ثقب",
                sku = "MAT-BRK-12",
                unit = "قطعة",
                quantityInStock = 2400.0,
                minThreshold = 3000.0,
                unitPrice = 42.0,
                supplierName = "مصنع إسمنت الشرق",
                warehouseLocation = "مخزن أ - علي منجلي"
            )
        )

        // Expenses
        db.expenseDao().insert(
            ExpenseEntity(
                title = "توريد 40 طن إسمنت للعمارة A",
                category = "مواد بناء",
                amount = 580000.0,
                date = "2026-11-16",
                projectName = "إقامة الياسمين السكنية (120 وحدة)",
                paymentMethod = "تحويل بنكي",
                notes = "الدفعة الخامسة للهيكل"
            )
        )
        db.expenseDao().insert(
            ExpenseEntity(
                title = "مستحقات أجور فرق البناء - النصف الأول",
                category = "أجور عمال",
                amount = 940000.0,
                date = "2026-11-15",
                projectName = "إقامة الياسمين السكنية (120 وحدة)",
                paymentMethod = "نقداً",
                notes = "أجور 34 عامل ومشرف"
            )
        )
        db.expenseDao().insert(
            ExpenseEntity(
                title = "وقود الديزل وصيانة الحفارة CAT 320",
                category = "وقود وصيانة",
                amount = 215000.0,
                date = "2026-11-14",
                projectName = "توسعة الطريق السريع المقطع الشرقي",
                paymentMethod = "نقداً",
                notes = "تغيير فلاتر وزيوت هيدروليك"
            )
        )
        db.expenseDao().insert(
            ExpenseEntity(
                title = "نقل معدات والواح سقالات لوهران",
                category = "نقل",
                amount = 135000.0,
                date = "2026-11-12",
                projectName = "المركز التجاري النخيل",
                paymentMethod = "شيك",
                notes = "شاحنتين نقل ثقيل"
            )
        )
        db.expenseDao().insert(
            ExpenseEntity(
                title = "رسوم دراسة هندسية وتأمين الورشات",
                category = "مصاريف إدارية",
                amount = 180000.0,
                date = "2026-11-10",
                projectName = "بناء ثانوية المستقبل النموذجية",
                paymentMethod = "تحويل بنكي",
                notes = "تأمين شامل للموقع"
            )
        )

        // Invoices
        db.invoiceDao().insert(
            InvoiceEntity(
                invoiceNumber = "INV-2026-101",
                type = "فاتورة عميل",
                partyName = "ديوان الترقية والتسيير العقاري",
                projectName = "إقامة الياسمين السكنية (120 وحدة)",
                totalAmount = 6500000.0,
                paidAmount = 6500000.0,
                issueDate = "2026-10-05",
                dueDate = "2026-11-05",
                status = "مدفوعة"
            )
        )
        db.invoiceDao().insert(
            InvoiceEntity(
                invoiceNumber = "INV-2026-108",
                type = "فاتورة عميل",
                partyName = "مديرية الأشغال العمومية",
                projectName = "توسعة الطريق السريع المقطع الشرقي",
                totalAmount = 4800000.0,
                paidAmount = 2400000.0,
                issueDate = "2026-11-01",
                dueDate = "2026-12-01",
                status = "مدفوعة جزئياً"
            )
        )
        db.invoiceDao().insert(
            InvoiceEntity(
                invoiceNumber = "INV-2026-112",
                type = "فاتورة عميل",
                partyName = "شركة الأمل للتطوير العقاري",
                projectName = "المركز التجاري النخيل",
                totalAmount = 3100000.0,
                paidAmount = 0.0,
                issueDate = "2026-10-12",
                dueDate = "2026-11-12",
                status = "متأخرة"
            )
        )
        db.invoiceDao().insert(
            InvoiceEntity(
                invoiceNumber = "SUP-2026-044",
                type = "فاتورة مورد",
                partyName = "مجمع الحديد المتحد",
                projectName = "إقامة الياسمين السكنية (120 وحدة)",
                totalAmount = 1420000.0,
                paidAmount = 600000.0,
                issueDate = "2026-11-08",
                dueDate = "2026-12-08",
                status = "مدفوعة جزئياً"
            )
        )

        // Schedule Tasks
        db.scheduleTaskDao().insert(
            ScheduleTaskEntity(
                title = "صب أعمدة الطابق السادس - العمارة B",
                projectName = "إقامة الياسمين السكنية (120 وحدة)",
                siteName = "ورشة العمارة A و B",
                assignedTeam = "فريق الخرسانة المسلحة",
                startDate = "2026-11-18",
                dueDate = "2026-12-05",
                priority = "عالية",
                isCompleted = false,
                progress = 45,
                assignedToUserId = 2L
            )
        )
        db.scheduleTaskDao().insert(
            ScheduleTaskEntity(
                title = "مراجعة مخططات الأساسات والتصريف الصحي",
                projectName = "توسعة الطريق السريع المقطع الشرقي",
                siteName = "ورشة المقطع الكيلومتري 14",
                assignedTeam = "المكتب الهندسي والرقابة",
                startDate = "2026-10-10",
                dueDate = "2026-10-28",
                priority = "عالية",
                isCompleted = false,
                progress = 35,
                assignedToUserId = 1L
            )
        )
        db.scheduleTaskDao().insert(
            ScheduleTaskEntity(
                title = "فرش طبقة الأساس الحصوية (كم 14 - 16)",
                projectName = "توسعة الطريق السريع المقطع الشرقي",
                siteName = "ورشة المقطع الكيلومتري 14",
                assignedTeam = "فرقة الآليات الثقيلة",
                startDate = "2026-11-01",
                dueDate = "2026-11-10",
                priority = "عالية",
                isCompleted = false,
                progress = 60,
                assignedToUserId = 2L
            )
        )
        db.scheduleTaskDao().insert(
            ScheduleTaskEntity(
                title = "اختبار التمديدات الكهربائية الرئيسية",
                projectName = "المركز التجاري النخيل",
                siteName = "ورشة التشطيبات والواجهات",
                assignedTeam = "فريق الكهرباء الصناعية",
                startDate = "2026-11-19",
                dueDate = "2026-12-12",
                priority = "متوسطة",
                isCompleted = false,
                progress = 20,
                assignedToUserId = 1L
            )
        )
        db.scheduleTaskDao().insert(
            ScheduleTaskEntity(
                title = "استلام شحنة الحديد وفحص الجودة",
                projectName = "إقامة الياسمين السكنية (120 وحدة)",
                siteName = "ورشة العمارة A و B",
                assignedTeam = "مراقبة الجودة والمخزن",
                startDate = "2026-11-10",
                dueDate = "2026-11-14",
                priority = "منخفضة",
                isCompleted = true,
                progress = 100,
                assignedToUserId = 2L
            )
        )
    }
}
