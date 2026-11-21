package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.BtpApplication

class BtpViewModelFactory(private val app: BtpApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val repo = app.container.repository
        val session = app.container.sessionManager
        val syncCoordinator = app.container.syncCoordinator
        val googleAuth = app.container.googleAuthManager
        val cloudSync = app.container.cloudSyncRepository
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(
                repo,
                session,
                googleAuth,
                cloudSync
            ) as T
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(repo) as T
            modelClass.isAssignableFrom(ProjectsViewModel::class.java) -> ProjectsViewModel(repo) as T
            modelClass.isAssignableFrom(ResourcesViewModel::class.java) -> ResourcesViewModel(repo) as T
            modelClass.isAssignableFrom(FinanceViewModel::class.java) -> FinanceViewModel(repo) as T
            modelClass.isAssignableFrom(ReportsViewModel::class.java) -> ReportsViewModel(repo) as T
            modelClass.isAssignableFrom(TopBarCenterViewModel::class.java) -> TopBarCenterViewModel(repo, syncCoordinator) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
