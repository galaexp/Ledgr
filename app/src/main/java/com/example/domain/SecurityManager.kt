package com.example.domain

import android.content.Context
import android.content.SharedPreferences

class SecurityManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ledgr_security_prefs", Context.MODE_PRIVATE)

    // Authentication & Onboarding
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean("is_logged_in", loggedIn).apply()
    }

    fun getUserName(): String {
        return prefs.getString("user_name", "") ?: ""
    }

    fun setUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    fun getUserEmail(): String {
        return prefs.getString("user_email", "") ?: ""
    }

    fun setUserEmail(email: String) {
        prefs.edit().putString("user_email", email).apply()
    }

    fun getUserCountryCode(): String {
        return prefs.getString("user_country_code", "US") ?: "US"
    }

    fun setUserCountryCode(code: String) {
        prefs.edit().putString("user_country_code", code).apply()
    }

    fun getUserCountryName(): String {
        return prefs.getString("user_country_name", "United States") ?: "United States"
    }

    fun setUserCountryName(name: String) {
        prefs.edit().putString("user_country_name", name).apply()
    }

    // PIN Protection
    fun isPinEnabled(): Boolean {
        return prefs.getBoolean("pin_enabled", false)
    }

    fun getStoredPin(): String {
        return prefs.getString("user_pin", "") ?: ""
    }

    fun setPin(pin: String) {
        prefs.edit()
            .putString("user_pin", pin)
            .putBoolean("pin_enabled", pin.length == 4)
            .apply()
    }

    fun removePin() {
        prefs.edit()
            .remove("user_pin")
            .putBoolean("pin_enabled", false)
            .apply()
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean("biometric_enabled", false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    // Currencies
    fun getPrimaryCountryCurrency(): String {
        return prefs.getString("primary_currency", "USD") ?: "USD"
    }

    fun setPrimaryCountryCurrency(currency: String) {
        prefs.edit().putString("primary_currency", currency).apply()
    }

    fun getSecondCountryCurrency(): String {
        return prefs.getString("second_currency", "EUR") ?: "EUR"
    }

    fun setSecondCountryCurrency(currency: String) {
        prefs.edit().putString("second_currency", currency).apply()
    }

    fun getSelectedThemePalette(): String {
        return prefs.getString("theme_palette", "EMERALD") ?: "EMERALD"
    }

    fun setSelectedThemePalette(palette: String) {
        prefs.edit().putString("theme_palette", palette).apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .apply()
    }
}
