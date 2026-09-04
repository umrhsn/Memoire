package com.umrhsn.mmoire.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.umrhsn.mmoire.models.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("memoire_prefs", Context.MODE_PRIVATE)

    fun isFirstTime(): Boolean {
        return prefs.getBoolean("is_first_time", true)
    }

    fun setFirstTime(value: Boolean) {
        prefs.edit { putBoolean("is_first_time", value) }
    }

    fun getLanguage(): String? {
        return prefs.getString("app_language", null)
    }

    fun setLanguage(lang: String?) {
        prefs.edit { putString("app_language", lang) }
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean("is_sound_enabled", true)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("is_sound_enabled", enabled) }
    }

    fun getTheme(): AppTheme {
        val themeName = prefs.getString("app_theme", AppTheme.SYSTEM.name)
        return try {
            AppTheme.valueOf(themeName ?: AppTheme.SYSTEM.name)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
    }

    fun setTheme(theme: AppTheme) {
        prefs.edit { putString("app_theme", theme.name) }
    }
}
