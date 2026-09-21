package com.umrhsn.mmoire.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umrhsn.mmoire.ui.screens.SettingsScreen
import com.umrhsn.mmoire.ui.theme.MemoireTheme
import com.umrhsn.mmoire.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("memoire_prefs", MODE_PRIVATE)
        val localeTag = prefs.getString("app_language", null)
        val context = if (localeTag != null) {
            val locale = Locale.forLanguageTag(localeTag)
            Locale.setDefault(locale)
            val config = newBase.resources.configuration
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            newBase.createConfigurationContext(config)
        } else {
            newBase
        }
        super.attachBaseContext(context)
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val windowSizeClass = calculateWindowSizeClass(this)

            MemoireTheme(
                appTheme = uiState.currentTheme,
                appColorTheme = uiState.currentColorTheme,
                isTintEnabled = uiState.isTintEnabled
            ) {
                SettingsScreen(
                    viewModel = viewModel,
                    windowSizeClass = windowSizeClass,
                    onBackClicked = { finish() }
                )
            }
        }
    }
}
