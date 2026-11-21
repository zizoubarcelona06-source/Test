package com.example.data.local

import com.example.core.security.SecurityUtils
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.EquipmentEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.MaterialEntity
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
                    isActive = true
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
                    isActive = true
                )
            )
        }
    }

    suspend fun ensureInitialDataSeeded(db: BtpDatabase) {
        ensureUsersSeeded(db)
        if (db.projectDao().count() > 0) return
        seedCompanySampleData(db)
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
                dueDate = "2026-11-21",
                priority = "عالية",
                isCompleted = false,
                progress = 45
            )
        )
        db.scheduleTaskDao().insert(
            ScheduleTaskEntity(
                title = "فرش طبقة الأساس الحصوية (كم 14 - 16)",
                projectName = "توسعة الطريق السريع المقطع الشرقي",
                siteName = "ورشة المقطع الكيلومتري 14",
                assignedTeam = "فرقة الآليات الثقيلة",
                startDate = "2026-11-17",
                dueDate = "2026-11-24",
                priority = "عالية",
                isCompleted = false,
                progress = 60
            )
        )
        db.scheduleTaskDao().insert(
            ScheduleTaskEntity(
                title = "اختبار التمديدات الكهربائية الرئيسية",
                projectName = "المركز التجاري النخيل",
                siteName = "ورشة التشطيبات والواجهات",
                assignedTeam = "فريق الكهرباء الصناعية",
                startDate = "2026-11-19",
                dueDate = "2026-11-23",
                priority = "متوسطة",
                isCompleted = false,
                progress = 20
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
                progress = 100
            )
        )
    }
}
