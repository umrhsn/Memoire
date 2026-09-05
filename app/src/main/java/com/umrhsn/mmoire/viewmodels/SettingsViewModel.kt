package com.umrhsn.mmoire.viewmodels

import androidx.lifecycle.ViewModel
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
    val currentTheme: AppTheme = AppTheme.SYSTEM
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
            currentTheme = prefs.getTheme()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateLanguage(lang: String?) {
        localeManager.applyLanguageTag(lang)
        _uiState.update { it.copy(currentLanguage = lang) }
    }

    fun toggleSound(enabled: Boolean) {
        prefs.setSoundEnabled(enabled)
        _uiState.update { it.copy(currentSoundEnabled = enabled) }
    }

    fun updateTheme(theme: AppTheme) {
        prefs.setTheme(theme)
        _uiState.update { it.copy(currentTheme = theme) }
    }
}
