package com.example

import android.app.Application
import com.example.core.security.SessionManager
import com.example.data.local.BtpDatabase
import com.example.data.repository.BtpRepository

class AppContainer(application: Application) {
    private val database: BtpDatabase by lazy { BtpDatabase.getInstance(application) }
    val repository: BtpRepository by lazy { BtpRepository(database) }
    val sessionManager: SessionManager by lazy { SessionManager(application) }
}

class BtpApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
