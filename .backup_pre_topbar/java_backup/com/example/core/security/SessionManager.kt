package com.example.core.security

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.btpDataStore by preferencesDataStore(name = "btp_manager_prefs")

data class AppPreferences(
    val loggedInUserId: Long? = null,
    val rememberSession: Boolean = true,
    val darkThemeOverride: Boolean? = null,
    val companyName: String = "شركة البنيان للأشغال العمومية",
    val currencySymbol: String = "د.ج",
    val languageCode: String = "ar"
)

class SessionManager(private val context: Context) {

    private object Keys {
        val LOGGED_IN_USER_ID = longPreferencesKey("logged_in_user_id")
        val REMEMBER_SESSION = booleanPreferencesKey("remember_session")
        val DARK_THEME_MODE = stringPreferencesKey("dark_theme_mode") // "system", "light", "dark"
        val COMPANY_NAME = stringPreferencesKey("company_name")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val LANGUAGE_CODE = stringPreferencesKey("app_language_code")
    }

    val preferencesFlow: Flow<AppPreferences> = context.btpDataStore.data.map { prefs ->
        val darkMode = when (prefs[Keys.DARK_THEME_MODE]) {
            "light" -> false
            "dark" -> true
            else -> null
        }
        AppPreferences(
            loggedInUserId = prefs[Keys.LOGGED_IN_USER_ID],
            rememberSession = prefs[Keys.REMEMBER_SESSION] ?: true,
            darkThemeOverride = darkMode,
            companyName = prefs[Keys.COMPANY_NAME] ?: "شركة البنيان للأشغال العمومية",
            currencySymbol = prefs[Keys.CURRENCY_SYMBOL] ?: "د.ج",
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "ar"
        )
    }

    suspend fun saveLoginSession(userId: Long, remember: Boolean) {
        context.btpDataStore.edit { prefs ->
            prefs[Keys.REMEMBER_SESSION] = remember
            if (remember) {
                prefs[Keys.LOGGED_IN_USER_ID] = userId
            } else {
                prefs.remove(Keys.LOGGED_IN_USER_ID)
            }
        }
    }

    suspend fun clearSession() {
        context.btpDataStore.edit { prefs ->
            prefs.remove(Keys.LOGGED_IN_USER_ID)
        }
    }

    suspend fun setThemeMode(darkOverride: Boolean?) {
        context.btpDataStore.edit { prefs ->
            prefs[Keys.DARK_THEME_MODE] = when (darkOverride) {
                true -> "dark"
                false -> "light"
                null -> "system"
            }
        }
    }

    suspend fun setLanguageCode(code: String) {
        context.btpDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE_CODE] = code
        }
    }

    suspend fun updateCompanySettings(companyName: String, currencySymbol: String) {
        context.btpDataStore.edit { prefs ->
            if (companyName.isNotBlank()) {
                prefs[Keys.COMPANY_NAME] = companyName.trim()
            }
            if (currencySymbol.isNotBlank()) {
                prefs[Keys.CURRENCY_SYMBOL] = currencySymbol.trim()
            }
        }
    }
}
