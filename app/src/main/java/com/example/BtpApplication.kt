package com.example

import android.app.Application
import com.example.core.security.GoogleAuthManager
import com.example.core.security.SessionManager
import com.example.core.sync.SyncCoordinator
import com.example.data.local.BtpDatabase
import com.example.data.remote.CloudSyncRepository
import com.example.data.repository.BtpRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(application: Application) {
    private val database: BtpDatabase by lazy { BtpDatabase.getInstance(application) }

    val sessionManager: SessionManager by lazy { SessionManager(application) }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(application.getString(R.string.firestore_database_id))
    }

    val googleAuthManager: GoogleAuthManager by lazy { GoogleAuthManager(application) }

    val cloudSyncRepository: CloudSyncRepository by lazy {
        CloudSyncRepository(
            firestore = firestore,
            auth = Firebase.auth,
            db = database,
            sessionManager = sessionManager
        )
    }

    val syncCoordinator: SyncCoordinator by lazy {
        SyncCoordinator(application, database) {
            cloudSyncRepository.pushPendingOutboxToCloud()
        }
    }

    val repository: BtpRepository by lazy {
        BtpRepository(database).also { repo ->
            repo.onOutboxChanged = {
                syncCoordinator.triggerAutoSync(force = false)
            }
        }
    }
}

class BtpApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
