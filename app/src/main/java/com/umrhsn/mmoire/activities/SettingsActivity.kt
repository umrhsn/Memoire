package com.umrhsn.mmoire.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umrhsn.mmoire.ui.screens.SettingsScreen
import com.umrhsn.mmoire.ui.theme.MemoireTheme
import com.umrhsn.mmoire.utils.LocaleContextWrapper
import com.umrhsn.mmoire.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("memoire_prefs", MODE_PRIVATE)
        val lang = prefs.getString("app_language", null)
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MemoireTheme(appTheme = uiState.currentTheme) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBackClicked = { finish() }
                )
            }
        }
    }
}
