package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ClientDao
import com.example.data.local.dao.EmployeeDao
import com.example.data.local.dao.EquipmentDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.InvoiceDao
import com.example.data.local.dao.MaterialDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.ScheduleTaskDao
import com.example.data.local.dao.SiteDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.dao.UserAccountDao
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
        ScheduleTaskEntity::class
    ],
    version = 2,
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

    companion object {
        @Volatile
        private var INSTANCE: BtpDatabase? = null

        fun getInstance(context: Context): BtpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BtpDatabase::class.java,
                    "btp_manager_offline.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
