package com.umrhsn.mmoire.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.umrhsn.mmoire.models.AppColorTheme
import com.umrhsn.mmoire.models.AppTheme
import com.umrhsn.mmoire.utils.LocaleManager
import com.umrhsn.mmoire.utils.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsUiState(
    val currentLanguage: String? = null,
    val currentSoundEnabled: Boolean = true,
    val currentTheme: AppTheme = AppTheme.SYSTEM,
    val currentColorTheme: AppColorTheme = AppColorTheme.DEFAULT,
    val isTintEnabled: Boolean = false,
    val pendingLanguage: String? = null,
    val showRestartDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PrefsManager,
    private val localeManager: LocaleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            currentLanguage = localeManager.getSelectedLanguageTag(),
            currentSoundEnabled = prefs.isSoundEnabled(),
            currentTheme = prefs.getTheme(),
            currentColorTheme = prefs.getColorTheme(),
            isTintEnabled = prefs.isBackgroundTintEnabled()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val TAG = "SettingsViewModel"

    fun updateLanguage(lang: String?) {
        Log.d(
            TAG,
            "updateLanguage: clicked on [$lang], current is [${_uiState.value.currentLanguage}]"
        )
        if (lang == _uiState.value.currentLanguage) return
        _uiState.update { it.copy(pendingLanguage = lang, showRestartDialog = true) }
    }

    fun confirmLanguageChange() {
        val lang = _uiState.value.pendingLanguage
        Log.d(TAG, "confirmLanguageChange: confirming change to [$lang]")
        localeManager.applyLanguageTag(lang)
        _uiState.update {
            it.copy(
                currentLanguage = lang,
                showRestartDialog = false,
                pendingLanguage = null
            )
        }
    }

    fun dismissRestartDialog() {
        Log.d(TAG, "dismissRestartDialog: cancelling language change")
        _uiState.update { it.copy(showRestartDialog = false, pendingLanguage = null) }
    }

    fun isLanguageSelected(tag: String?): Boolean {
        return _uiState.value.currentLanguage == tag
    }

    fun toggleSound(enabled: Boolean) {
        prefs.setSoundEnabled(enabled)
        _uiState.update { it.copy(currentSoundEnabled = enabled) }
    }

    fun updateTheme(theme: AppTheme) {
        prefs.setTheme(theme)
        _uiState.update { it.copy(currentTheme = theme) }
    }

    fun updateColorTheme(theme: AppColorTheme) {
        prefs.setColorTheme(theme)
        _uiState.update { it.copy(currentColorTheme = theme) }
    }

    fun toggleBackgroundTint(enabled: Boolean) {
        prefs.setBackgroundTintEnabled(enabled)
        _uiState.update { it.copy(isTintEnabled = enabled) }
    }
}
