package com.example.pesatrack.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pesatrack_prefs")

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String,
    val country: String,
    val flag: String
)

object Currencies {
    val list = listOf(
        CurrencyInfo("KES", "KSh", "Kenyan Shilling", "Kenya", "🇰🇪"),
        CurrencyInfo("USD", "$", "US Dollar", "United States", "🇺🇸"),
        CurrencyInfo("UGX", "USh", "Ugandan Shilling", "Uganda", "🇺🇬"),
        CurrencyInfo("TZS", "TSh", "Tanzanian Shilling", "Tanzania", "🇹🇿"),
        CurrencyInfo("RWF", "RF", "Rwandan Franc", "Rwanda", "🇷🇼"),
        CurrencyInfo("NGN", "₦", "Nigerian Naira", "Nigeria", "🇳🇬"),
        CurrencyInfo("GHS", "GH₵", "Ghanaian Cedi", "Ghana", "🇬🇭"),
        CurrencyInfo("ZAR", "R", "South African Rand", "South Africa", "🇿🇦"),
        CurrencyInfo("GBP", "£", "British Pound", "United Kingdom", "🇬🇧"),
        CurrencyInfo("EUR", "€", "Euro", "European Union", "🇪🇺"),
        CurrencyInfo("CAD", "CA$", "Canadian Dollar", "Canada", "🇨🇦"),
        CurrencyInfo("INR", "₹", "Indian Rupee", "India", "🇮🇳"),
        CurrencyInfo("AUD", "AU$", "Australian Dollar", "Australia", "🇦🇺")
    )

    fun getByCode(code: String): CurrencyInfo {
        return list.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: list[0]
    }

    fun getByCountry(country: String): CurrencyInfo {
        return list.firstOrNull { it.country.equals(country, ignoreCase = true) } ?: list[0]
    }
}

data class UserProfile(
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val country: String,
    val isRegistered: Boolean,
    val isLoggedIn: Boolean
)

class UserPreferencesManager(private val context: Context) {

    companion object {
        val KEY_CURRENCY_CODE = stringPreferencesKey("currency_code")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_PHONE_NUMBER = stringPreferencesKey("phone_number")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_COUNTRY = stringPreferencesKey("country")
        val KEY_PASSWORD_HASH = stringPreferencesKey("password_hash")
        val KEY_IS_REGISTERED = booleanPreferencesKey("is_registered")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_BUDGET_ALERTS_ENABLED = booleanPreferencesKey("budget_alerts_enabled")
        val KEY_GOAL_REMINDERS_ENABLED = booleanPreferencesKey("goal_reminders_enabled")
    }

    val isRegisteredFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_REGISTERED] ?: false
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            fullName = prefs[KEY_USER_NAME] ?: "",
            phoneNumber = prefs[KEY_PHONE_NUMBER] ?: "",
            email = prefs[KEY_EMAIL] ?: "",
            country = prefs[KEY_COUNTRY] ?: "Kenya",
            isRegistered = prefs[KEY_IS_REGISTERED] ?: false,
            isLoggedIn = prefs[KEY_IS_LOGGED_IN] ?: false
        )
    }

    val storedPasswordHashFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PASSWORD_HASH] ?: ""
    }

    val currencyCodeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CURRENCY_CODE] ?: "KES"
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: "Student"
    }

    val budgetAlertsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BUDGET_ALERTS_ENABLED] ?: true
    }

    val goalRemindersEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_GOAL_REMINDERS_ENABLED] ?: true
    }

    suspend fun registerUser(
        fullName: String,
        phoneNumber: String,
        email: String,
        country: String,
        currencyCode: String,
        passwordHash: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = fullName
            prefs[KEY_PHONE_NUMBER] = phoneNumber
            prefs[KEY_EMAIL] = email
            prefs[KEY_COUNTRY] = country
            prefs[KEY_CURRENCY_CODE] = currencyCode
            prefs[KEY_PASSWORD_HASH] = passwordHash
            prefs[KEY_IS_REGISTERED] = true
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = loggedIn
        }
    }

    suspend fun setCurrency(currencyCode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CURRENCY_CODE] = currencyCode
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
        }
    }

    suspend fun setBudgetAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BUDGET_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun setGoalRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GOAL_REMINDERS_ENABLED] = enabled
        }
    }
}
