package com.umrhsn.mmoire.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umrhsn.mmoire.ui.screens.SettingsScreen
import com.umrhsn.mmoire.ui.theme.MemoireTheme
import com.umrhsn.mmoire.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Handle Success (Apply Clicked)
            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    val appLocales = if (uiState.currentLanguage != null) {
                        LocaleListCompat.forLanguageTags(uiState.currentLanguage)
                    } else {
                        LocaleListCompat.getEmptyLocaleList()
                    }
                    AppCompatDelegate.setApplicationLocales(appLocales)
                    finish()
                }
            }

            MemoireTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onBackClicked = { finish() }
                )
            }
        }
    }
}
