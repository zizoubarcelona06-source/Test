package com.example.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.localization.AppLanguage
import com.example.core.security.AppPreferences
import com.example.core.security.GoogleAuthManager
import com.example.core.security.GoogleSignInOutcome
import com.example.core.security.SessionManager
import com.example.data.local.entity.UserAccountEntity
import com.example.data.remote.CloudSyncRepository
import com.example.data.remote.CloudWorkspaceState
import com.example.data.remote.FsCompanyMember
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
    private val sessionManager: SessionManager,
    private val googleAuthManager: GoogleAuthManager,
    private val cloudSyncRepository: CloudSyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val preferences: StateFlow<AppPreferences> = sessionManager.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences())

    val allUsers: StateFlow<List<UserAccountEntity>> = repository.users
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workspaceState: StateFlow<CloudWorkspaceState> = cloudSyncRepository.workspaceState
    val companyMembers: StateFlow<List<FsCompanyMember>> = cloudSyncRepository.companyMembers

    private val _workspaceBannerDismissed = MutableStateFlow(false)
    val workspaceBannerDismissed: StateFlow<Boolean> = _workspaceBannerDismissed.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.initializeOfflineData()
                val firebaseUser = googleAuthManager.currentFirebaseUser
                if (firebaseUser != null) {
                    val synced = cloudSyncRepository.syncGoogleUserOnSignIn(firebaseUser)
                    val syncedUser = synced.getOrNull()
                    if (syncedUser != null) {
                        registerDevice(syncedUser.id)
                        _uiState.update {
                            it.copy(
                                isInitializing = false,
                                currentUser = syncedUser
                            )
                        }
                        return@launch
                    }
                }

                val prefs = sessionManager.preferencesFlow.first()
                val restoredUser = prefs.loggedInUserId?.let { id ->
                    repository.getUserById(id)?.takeIf { it.isActive }
                }
                if (restoredUser != null) {
                    registerDevice(restoredUser.id)
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

    private suspend fun registerDevice(userId: Long) {
        val brand = (Build.MANUFACTURER ?: "Android").replaceFirstChar { it.uppercase() }
        val model = Build.MODEL ?: "Device"
        val deviceName = if (model.startsWith(brand, ignoreCase = true)) model else "$brand $model"
        val release = Build.VERSION.RELEASE ?: "14"
        repository.registerCurrentDeviceSession(
            userId = userId,
            deviceName = deviceName,
            deviceModel = "Android $release"
        )
    }

    fun attemptAutoSignIn(activityContext: Context) {
        if (googleAuthManager.currentFirebaseUser != null || _uiState.value.currentUser != null) return
        viewModelScope.launch {
            when (val outcome = googleAuthManager.attemptAutoSignIn(activityContext)) {
                is GoogleSignInOutcome.Success -> {
                    cloudSyncRepository.syncGoogleUserOnSignIn(outcome.user).onSuccess { localUser ->
                        registerDevice(localUser.id)
                        _workspaceBannerDismissed.value = false
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentUser = localUser,
                                errorMessage = null
                            )
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, statusMessage = null) }
            when (val outcome = googleAuthManager.signInWithGoogle(activityContext)) {
                is GoogleSignInOutcome.Success -> {
                    val syncRes = cloudSyncRepository.syncGoogleUserOnSignIn(outcome.user)
                    syncRes.onSuccess { localUser ->
                        registerDevice(localUser.id)
                        _workspaceBannerDismissed.value = false
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentUser = localUser,
                                errorMessage = null,
                                statusMessage = null
                            )
                        }
                    }.onFailure { err ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = err.message ?: "ERR_GOOGLE_SIGN_IN"
                            )
                        }
                    }
                }
                GoogleSignInOutcome.Cancelled -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
                GoogleSignInOutcome.NoSavedCredential,
                is GoogleSignInOutcome.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "ERR_GOOGLE_SIGN_IN"
                        )
                    }
                }
            }
        }
    }

    fun createCompanyWorkspace(
        companyName: String,
        currencySymbol: String,
        defaultInviteRole: String = "مهندس موقع"
    ) {
        val current = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = cloudSyncRepository.createCompanyWorkspace(
                companyName = companyName,
                currencySymbol = currencySymbol,
                defaultInviteRole = defaultInviteRole,
                currentUser = current
            )
            result.onSuccess {
                val refreshed = repository.getUserById(current.id) ?: current.copy(role = "مدير عام")
                _workspaceBannerDismissed.value = false
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = refreshed,
                        statusMessage = "MSG_COMPANY_CREATED",
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "ERR_GENERIC_AUTH"
                    )
                }
            }
        }
    }

    fun joinCompanyByInviteCode(inviteCode: String) {
        val current = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = cloudSyncRepository.joinCompanyByInviteCode(inviteCode, current)
            result.onSuccess {
                val refreshed = repository.getUserById(current.id) ?: current
                _workspaceBannerDismissed.value = false
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = refreshed,
                        statusMessage = "MSG_COMPANY_JOINED",
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "ERR_INVALID_INVITE_CODE"
                    )
                }
            }
        }
    }

    fun dismissWorkspaceSetupForNow() {
        _workspaceBannerDismissed.value = true
    }

    fun reopenWorkspaceSetup() {
        _workspaceBannerDismissed.value = false
    }

    fun updateCloudMemberRole(memberUid: String, newRole: String) {
        viewModelScope.launch {
            cloudSyncRepository.updateMemberRole(memberUid, newRole).onSuccess {
                _uiState.update { it.copy(statusMessage = "MSG_USER_STATUS_UPDATED") }
            }
        }
    }

    fun updateInviteDefaultRole(newRole: String) {
        viewModelScope.launch {
            cloudSyncRepository.updateInviteDefaultRole(newRole)
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
                        registerDevice(result.user.id)
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
            googleAuthManager.signOut {
                cloudSyncRepository.stopCloudSync()
            }
            sessionManager.clearSession()
            _workspaceBannerDismissed.value = false
            _uiState.update { it.copy(currentUser = null, errorMessage = null, statusMessage = null) }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, statusMessage = null) }
    }

    fun setLanguage(language: AppLanguage) {
        val current = _uiState.value.currentUser
        viewModelScope.launch {
            if (current != null) {
                val updated = repository.updateUserLanguage(current.id, language.code)
                    ?: current.copy(preferredLanguageCode = language.code)
                _uiState.update { it.copy(currentUser = updated) }
                cloudSyncRepository.syncUserPreferencesToCloud(updated)
            } else {
                sessionManager.setLanguageCode(language.code)
            }
        }
    }

    fun updateCurrentUserProfile(fullName: String, branchName: String, avatarUri: String) {
        val current = _uiState.value.currentUser ?: return
        if (fullName.isBlank()) return
        viewModelScope.launch {
            val updated = repository.updateUserProfile(current.id, fullName, branchName, avatarUri)
                ?: current.copy(
                    fullName = fullName.trim(),
                    branchName = branchName.trim(),
                    avatarUri = avatarUri
                )
            _uiState.update { it.copy(currentUser = updated, statusMessage = "MSG_PROFILE_UPDATED") }
            cloudSyncRepository.syncUserPreferencesToCloud(updated)
        }
    }

    fun verifyAndChangePassword(
        currentPassword: String,
        newPassword: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val current = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val result = repository.verifyAndChangePassword(current.id, currentPassword, newPassword)
            result.onSuccess {
                val refreshed = repository.getUserById(current.id) ?: current
                _uiState.update { it.copy(currentUser = refreshed, statusMessage = "MSG_PASSWORD_UPDATED", errorMessage = null) }
                onComplete(true, null)
            }.onFailure { err ->
                onComplete(false, err.message)
            }
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
