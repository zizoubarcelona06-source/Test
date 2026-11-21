package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

object BtpConstants {
    val userRoles = listOf("مدير عام", "مدير مشاريع", "محاسب مالي", "مهندس موقع")
    val projectStatuses = listOf("قيد التنفيذ", "مخطط", "متوقف", "مكتمل")
    val siteStatuses = listOf("نشط", "صيانة", "مغلق")
    val safetyStatuses = listOf("آمن", "تنبيه", "طارئ")
    val employeeRoles = listOf("مهندس مدني", "مراقب ورشة", "رئيس بناء", "سائق آليات", "كهربائي", "عامل متخصص")
    val attendanceStatuses = listOf("حاضر", "غائب", "إجازة")
    val equipmentCategories = listOf("حفارة", "رافعة", "شاحنة", "خلاطة", "ضاغط هوائي")
    val equipmentStatuses = listOf("في الخدمة", "صيانة", "متاح")
    val materialUnits = listOf("طن", "كيس", "م³", "متر", "قطعة", "لتر")
    val expenseCategories = listOf("مواد بناء", "أجور عمال", "وقود وصيانة", "نقل", "مصاريف إدارية")
    val paymentMethods = listOf("نقداً", "تحويل بنكي", "شيك")
    val invoiceTypes = listOf("فاتورة عميل", "فاتورة مورد")
    val invoiceStatuses = listOf("مدفوعة", "مدفوعة جزئياً", "غير مدفوعة", "متأخرة")
    val supplierCategories = listOf("خرسانة وإسمنت", "حديد وصلب", "معدات وآليات", "كهرباء وسباكة", "نقل ولوجستيك")
    val taskPriorities = listOf("عالية", "متوسطة", "منخفضة")
}

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val username: String,
    val phone: String = "",
    val role: String,
    val passwordHash: String,
    val salt: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null
)

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val companyName: String,
    val phone: String,
    val email: String,
    val address: String,
    val taxId: String,
    val notes: String = ""
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val phone: String,
    val email: String,
    val address: String,
    val balanceOwed: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val clientName: String,
    val location: String,
    val budget: Double,
    val spentAmount: Double = 0.0,
    val startDate: String,
    val endDate: String,
    val progressPercent: Int = 0,
    val status: String = "قيد التنفيذ",
    val description: String = ""
)

@Entity(tableName = "sites")
data class SiteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long? = null,
    val projectName: String,
    val name: String,
    val address: String,
    val supervisorName: String,
    val workerCount: Int = 0,
    val safetyStatus: String = "آمن",
    val status: String = "نشط",
    val dailyNote: String = ""
)

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val role: String,
    val phone: String,
    val nationalId: String,
    val dailyWage: Double,
    val assignedSiteName: String,
    val attendanceStatus: String = "حاضر",
    val hireDate: String,
    val isActive: Boolean = true
)

@Entity(tableName = "equipment")
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val category: String,
    val status: String = "في الخدمة",
    val assignedSiteName: String,
    val nextMaintenanceDate: String,
    val hourlyCost: Double = 0.0
)

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String,
    val unit: String,
    val quantityInStock: Double,
    val minThreshold: Double,
    val unitPrice: Double,
    val supplierName: String,
    val warehouseLocation: String
) {
    val isLowStock: Boolean
        get() = quantityInStock <= minThreshold
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val amount: Double,
    val date: String,
    val projectName: String,
    val paymentMethod: String,
    val notes: String = ""
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val type: String, // فاتورة عميل | فاتورة مورد
    val partyName: String,
    val projectName: String,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val issueDate: String,
    val dueDate: String,
    val status: String // مدفوعة | مدفوعة جزئياً | غير مدفوعة | متأخرة
) {
    val remainingAmount: Double
        get() = (totalAmount - paidAmount).coerceAtLeast(0.0)
}

@Entity(tableName = "schedule_tasks")
data class ScheduleTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val projectName: String,
    val siteName: String,
    val assignedTeam: String,
    val startDate: String,
    val dueDate: String,
    val priority: String = "متوسطة",
    val isCompleted: Boolean = false,
    val progress: Int = 0
)
