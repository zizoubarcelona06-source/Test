package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Delete
    suspend fun delete(user: UserAccountEntity)
}

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY id DESC")
    fun observeAll(): Flow<List<ClientEntity>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: ScheduleTaskEntity): Long

    @Update
    suspend fun update(task: ScheduleTaskEntity)

    @Delete
    suspend fun delete(task: ScheduleTaskEntity)

    @Query("DELETE FROM schedule_tasks")
    suspend fun deleteAll()
}
