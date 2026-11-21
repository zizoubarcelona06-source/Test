package com.example.data.repository

import com.example.core.security.SecurityUtils
import com.example.data.local.BtpDatabase
import com.example.data.local.DatabaseSeeder
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
import kotlinx.coroutines.flow.Flow

sealed class AuthResult {
    data class Success(val user: UserAccountEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class BtpRepository(private val db: BtpDatabase) {

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
        return Result.success(id)
    }

    suspend fun changePassword(userId: Long, newPassword: String) {
        val user = db.userAccountDao().getById(userId) ?: return
        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPassword, newSalt)
        db.userAccountDao().update(user.copy(passwordHash = newHash, salt = newSalt))
    }

    suspend fun toggleUserActive(user: UserAccountEntity) {
        db.userAccountDao().update(user.copy(isActive = !user.isActive))
    }

    // Projects
    val projects: Flow<List<ProjectEntity>> = db.projectDao().observeAll()
    suspend fun saveProject(project: ProjectEntity) {
        if (project.id == 0L) db.projectDao().insert(project) else db.projectDao().update(project)
    }
    suspend fun deleteProject(project: ProjectEntity) = db.projectDao().delete(project)

    // Sites
    val sites: Flow<List<SiteEntity>> = db.siteDao().observeAll()
    suspend fun saveSite(site: SiteEntity) {
        if (site.id == 0L) db.siteDao().insert(site) else db.siteDao().update(site)
    }
    suspend fun deleteSite(site: SiteEntity) = db.siteDao().delete(site)

    // Schedule Tasks
    val scheduleTasks: Flow<List<ScheduleTaskEntity>> = db.scheduleTaskDao().observeAll()
    suspend fun saveScheduleTask(task: ScheduleTaskEntity) {
        if (task.id == 0L) db.scheduleTaskDao().insert(task) else db.scheduleTaskDao().update(task)
    }
    suspend fun deleteScheduleTask(task: ScheduleTaskEntity) = db.scheduleTaskDao().delete(task)

    // Employees
    val employees: Flow<List<EmployeeEntity>> = db.employeeDao().observeAll()
    suspend fun saveEmployee(employee: EmployeeEntity) {
        if (employee.id == 0L) db.employeeDao().insert(employee) else db.employeeDao().update(employee)
    }
    suspend fun deleteEmployee(employee: EmployeeEntity) = db.employeeDao().delete(employee)

    // Equipment
    val equipment: Flow<List<EquipmentEntity>> = db.equipmentDao().observeAll()
    suspend fun saveEquipment(item: EquipmentEntity) {
        if (item.id == 0L) db.equipmentDao().insert(item) else db.equipmentDao().update(item)
    }
    suspend fun deleteEquipment(item: EquipmentEntity) = db.equipmentDao().delete(item)

    // Materials
    val materials: Flow<List<MaterialEntity>> = db.materialDao().observeAll()
    suspend fun saveMaterial(material: MaterialEntity) {
        if (material.id == 0L) db.materialDao().insert(material) else db.materialDao().update(material)
    }
    suspend fun deleteMaterial(material: MaterialEntity) = db.materialDao().delete(material)

    // Expenses
    val expenses: Flow<List<ExpenseEntity>> = db.expenseDao().observeAll()
    suspend fun saveExpense(expense: ExpenseEntity) {
        if (expense.id == 0L) db.expenseDao().insert(expense) else db.expenseDao().update(expense)
    }
    suspend fun deleteExpense(expense: ExpenseEntity) = db.expenseDao().delete(expense)

    // Invoices
    val invoices: Flow<List<InvoiceEntity>> = db.invoiceDao().observeAll()
    suspend fun saveInvoice(invoice: InvoiceEntity) {
        if (invoice.id == 0L) db.invoiceDao().insert(invoice) else db.invoiceDao().update(invoice)
    }
    suspend fun recordInvoicePayment(invoice: InvoiceEntity, addedAmount: Double) {
        val newPaid = (invoice.paidAmount + addedAmount).coerceIn(0.0, invoice.totalAmount)
        val newStatus = when {
            newPaid >= invoice.totalAmount -> "مدفوعة"
            newPaid > 0.0 -> "مدفوعة جزئياً"
            else -> "غير مدفوعة"
        }
        db.invoiceDao().update(invoice.copy(paidAmount = newPaid, status = newStatus))
    }
    suspend fun deleteInvoice(invoice: InvoiceEntity) = db.invoiceDao().delete(invoice)

    // Clients
    val clients: Flow<List<ClientEntity>> = db.clientDao().observeAll()
    suspend fun saveClient(client: ClientEntity) {
        if (client.id == 0L) db.clientDao().insert(client) else db.clientDao().update(client)
    }
    suspend fun deleteClient(client: ClientEntity) = db.clientDao().delete(client)

    // Suppliers
    val suppliers: Flow<List<SupplierEntity>> = db.supplierDao().observeAll()
    suspend fun saveSupplier(supplier: SupplierEntity) {
        if (supplier.id == 0L) db.supplierDao().insert(supplier) else db.supplierDao().update(supplier)
    }
    suspend fun deleteSupplier(supplier: SupplierEntity) = db.supplierDao().delete(supplier)
}
