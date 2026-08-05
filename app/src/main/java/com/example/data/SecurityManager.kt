package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SecurityManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    fun isPinEnabled(): Boolean {
        return prefs.getBoolean("pin_enabled", false)
    }

    fun getPinCode(): String {
        return prefs.getString("pin_code", "") ?: ""
    }

    fun enablePin(pin: String) {
        prefs.edit()
            .putBoolean("pin_enabled", true)
            .putString("pin_code", pin)
            .apply()
    }

    fun disablePin() {
        prefs.edit()
            .putBoolean("pin_enabled", false)
            .putString("pin_code", "")
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        return pin == getPinCode()
    }
}
