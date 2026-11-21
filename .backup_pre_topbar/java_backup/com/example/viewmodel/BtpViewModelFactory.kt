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
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repo, session) as T
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(repo) as T
            modelClass.isAssignableFrom(ProjectsViewModel::class.java) -> ProjectsViewModel(repo) as T
            modelClass.isAssignableFrom(ResourcesViewModel::class.java) -> ResourcesViewModel(repo) as T
            modelClass.isAssignableFrom(FinanceViewModel::class.java) -> FinanceViewModel(repo) as T
            modelClass.isAssignableFrom(ReportsViewModel::class.java) -> ReportsViewModel(repo) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
