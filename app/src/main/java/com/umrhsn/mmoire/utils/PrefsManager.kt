package com.umrhsn.mmoire.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.umrhsn.mmoire.models.AppColorTheme
import com.umrhsn.mmoire.models.AppTheme
import com.umrhsn.mmoire.models.TwoPlayerLayout
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("memoire_prefs", Context.MODE_PRIVATE)

    private val _themeFlow = MutableStateFlow(getThemeInternal())
    val themeFlow: StateFlow<AppTheme> = _themeFlow.asStateFlow()

    private val _colorThemeFlow = MutableStateFlow(getColorThemeInternal())
    val colorThemeFlow: StateFlow<AppColorTheme> = _colorThemeFlow.asStateFlow()

    private val _tintFlow = MutableStateFlow(isBackgroundTintEnabled())
    val tintFlow: StateFlow<Boolean> = _tintFlow.asStateFlow()

    private val _twoPlayerLayoutFlow = MutableStateFlow(getTwoPlayerLayoutInternal())
    val twoPlayerLayoutFlow: StateFlow<TwoPlayerLayout> = _twoPlayerLayoutFlow.asStateFlow()

    private val _localeFlow = MutableStateFlow(getLanguage())
    val localeFlow: StateFlow<String?> = _localeFlow.asStateFlow()

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
        _localeFlow.value = lang
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean("is_sound_enabled", true)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("is_sound_enabled", enabled) }
    }

    fun getTheme(): AppTheme {
        return _themeFlow.value
    }

    private fun getThemeInternal(): AppTheme {
        val themeName = prefs.getString("app_theme", AppTheme.SYSTEM.name)
        return try {
            AppTheme.valueOf(themeName ?: AppTheme.SYSTEM.name)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
    }

    fun setTheme(theme: AppTheme) {
        prefs.edit { putString("app_theme", theme.name) }
        _themeFlow.value = theme
    }

    fun getColorTheme(): AppColorTheme {
        return _colorThemeFlow.value
    }

    private fun getColorThemeInternal(): AppColorTheme {
        val themeName = prefs.getString("app_color_theme", AppColorTheme.DEFAULT.name)
        return try {
            AppColorTheme.valueOf(themeName ?: AppColorTheme.DEFAULT.name)
        } catch (e: Exception) {
            AppColorTheme.DEFAULT
        }
    }

    fun setColorTheme(theme: AppColorTheme) {
        prefs.edit { putString("app_color_theme", theme.name) }
        _colorThemeFlow.value = theme
    }

    fun isBackgroundTintEnabled(): Boolean {
        return prefs.getBoolean("bg_tint_enabled", false)
    }

    fun setBackgroundTintEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("bg_tint_enabled", enabled) }
        _tintFlow.value = enabled
    }

    fun getTwoPlayerLayout(): TwoPlayerLayout {
        return _twoPlayerLayoutFlow.value
    }

    private fun getTwoPlayerLayoutInternal(): TwoPlayerLayout {
        val name = prefs.getString("two_player_layout", TwoPlayerLayout.FACE_TO_FACE.name)
        return try {
            TwoPlayerLayout.valueOf(name ?: TwoPlayerLayout.FACE_TO_FACE.name)
        } catch (e: Exception) {
            TwoPlayerLayout.FACE_TO_FACE
        }
    }

    fun setTwoPlayerLayout(layout: TwoPlayerLayout) {
        prefs.edit { putString("two_player_layout", layout.name) }
        _twoPlayerLayoutFlow.value = layout
    }
}
