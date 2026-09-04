package com.umrhsn.mmoire.viewmodels

import androidx.lifecycle.ViewModel
import com.umrhsn.mmoire.models.AppTheme
import com.umrhsn.mmoire.utils.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsUiState(
    val initialLanguage: String? = null,
    val initialSoundEnabled: Boolean = true,
    val initialTheme: AppTheme = AppTheme.SYSTEM,
    val currentLanguage: String? = null,
    val currentSoundEnabled: Boolean = true,
    val currentTheme: AppTheme = AppTheme.SYSTEM,
    val isSuccess: Boolean = false
) {
    val hasChanges: Boolean
        get() = currentLanguage != initialLanguage ||
                currentSoundEnabled != initialSoundEnabled ||
                currentTheme != initialTheme
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            initialLanguage = prefs.getLanguage(),
            initialSoundEnabled = prefs.isSoundEnabled(),
            initialTheme = prefs.getTheme(),
            currentLanguage = prefs.getLanguage(),
            currentSoundEnabled = prefs.isSoundEnabled(),
            currentTheme = prefs.getTheme()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateLanguage(lang: String?) {
        _uiState.update { it.copy(currentLanguage = lang) }
    }

    fun toggleSound(enabled: Boolean) {
        _uiState.update { it.copy(currentSoundEnabled = enabled) }
    }

    fun updateTheme(theme: AppTheme) {
        _uiState.update { it.copy(currentTheme = theme) }
    }

    fun applyChanges() {
        val state = _uiState.value
        prefs.setLanguage(state.currentLanguage)
        prefs.setSoundEnabled(state.currentSoundEnabled)
        prefs.setTheme(state.currentTheme)
        _uiState.update { it.copy(isSuccess = true) }
    }
}
