package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.localization.AppLanguage
import com.example.core.security.AppPreferences
import com.example.core.security.SessionManager
import com.example.data.local.entity.UserAccountEntity
import com.example.data.repository.AuthResult
import com.example.data.repository.BtpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isInitializing: Boolean = true,
    val currentUser: UserAccountEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null
)

class AuthViewModel(
    private val repository: BtpRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val preferences: StateFlow<AppPreferences> = sessionManager.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences())

    val allUsers: StateFlow<List<UserAccountEntity>> = repository.users
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            try {
                repository.initializeOfflineData()
                val prefs = sessionManager.preferencesFlow.first()
                val restoredUser = prefs.loggedInUserId?.let { id ->
                    repository.getUserById(id)?.takeIf { it.isActive }
                }
                _uiState.update {
                    it.copy(
                        isInitializing = false,
                        currentUser = restoredUser
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isInitializing = false) }
            }
        }
    }

    fun login(phoneOrUsername: String, password: String, rememberMe: Boolean) {
        val cleanedId = phoneOrUsername.trim().replace(" ", "").replace("-", "")
        if (cleanedId.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "ERR_EMPTY_PHONE") }
            return
        }
        val isUsernameFallback = cleanedId.equals("admin", ignoreCase = true) ||
            cleanedId.equals("engineer", ignoreCase = true) ||
            cleanedId.any { it.isLetter() }
        if (!isUsernameFallback) {
            val digitsOnly = cleanedId.removePrefix("+")
            if (digitsOnly.length < 8 || !digitsOnly.all { it.isDigit() }) {
                _uiState.update { it.copy(errorMessage = "ERR_INVALID_PHONE") }
                return
            }
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "ERR_EMPTY_PASSWORD") }
            return
        }
        if (password.length < 4) {
            _uiState.update { it.copy(errorMessage = "ERR_SHORT_PASSWORD") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                when (val result = repository.authenticate(cleanedId, password)) {
                    is AuthResult.Success -> {
                        sessionManager.saveLoginSession(result.user.id, rememberMe)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentUser = result.user,
                                errorMessage = null,
                                statusMessage = null
                            )
                        }
                    }
                    is AuthResult.Error -> {
                        val key = if (result.message.contains("معطل")) "ERR_ACCOUNT_DISABLED" else "ERR_INVALID_CREDENTIALS"
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = key
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "ERR_GENERIC_AUTH"
                    )
                }
            }
        }
    }

    fun loginDemo(phone: String = "0550000001", password: String = "123456", rememberMe: Boolean = true) {
        login(phone, password, rememberMe)
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _uiState.update { it.copy(currentUser = null, errorMessage = null, statusMessage = null) }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, statusMessage = null) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            sessionManager.setLanguageCode(language.code)
        }
    }

    fun createUser(fullName: String, username: String, phone: String, role: String, password: String) {
        val cleanPhone = phone.trim().replace(" ", "").replace("-", "")
        if (fullName.isBlank() || username.isBlank() || cleanPhone.length < 8 || password.length < 4) {
            _uiState.update { it.copy(errorMessage = "ERR_CREATE_USER_FIELDS") }
            return
        }
        viewModelScope.launch {
            val result = repository.createUser(fullName, username, cleanPhone, role, password)
            result.onSuccess {
                _uiState.update { it.copy(statusMessage = "MSG_USER_CREATED", errorMessage = null) }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = "ERR_USER_EXISTS") }
            }
        }
    }

    fun changeCurrentPassword(newPassword: String) {
        val current = _uiState.value.currentUser ?: return
        if (newPassword.length < 4) {
            _uiState.update { it.copy(errorMessage = "ERR_SHORT_PASSWORD") }
            return
        }
        viewModelScope.launch {
            repository.changePassword(current.id, newPassword)
            _uiState.update { it.copy(statusMessage = "MSG_PASSWORD_UPDATED", errorMessage = null) }
        }
    }

    fun toggleUserActive(user: UserAccountEntity) {
        if (user.id == _uiState.value.currentUser?.id) {
            _uiState.update { it.copy(errorMessage = "ERR_DISABLE_ACTIVE_USER") }
            return
        }
        viewModelScope.launch {
            repository.toggleUserActive(user)
            _uiState.update { it.copy(statusMessage = "MSG_USER_STATUS_UPDATED") }
        }
    }

    fun setDarkThemeMode(darkOverride: Boolean?) {
        viewModelScope.launch {
            sessionManager.setThemeMode(darkOverride)
        }
    }

    fun updateCompanySettings(companyName: String, currency: String) {
        viewModelScope.launch {
            sessionManager.updateCompanySettings(companyName, currency)
            _uiState.update { it.copy(statusMessage = "MSG_COMPANY_SAVED") }
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetSampleData()
            _uiState.update { it.copy(statusMessage = "MSG_DEMO_RESTORED") }
        }
    }
}
