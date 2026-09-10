package com.umrhsn.mmoire.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umrhsn.mmoire.ui.screens.SettingsScreen
import com.umrhsn.mmoire.ui.theme.MemoireTheme
import com.umrhsn.mmoire.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
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
