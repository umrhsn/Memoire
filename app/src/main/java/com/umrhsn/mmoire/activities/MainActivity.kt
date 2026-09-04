package com.umrhsn.mmoire.activities

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umrhsn.mmoire.activities.BrowseActivity
import com.umrhsn.mmoire.activities.CreateActivity
import com.umrhsn.mmoire.activities.SettingsActivity
import com.umrhsn.mmoire.ui.screens.MainScreen
import com.umrhsn.mmoire.ui.theme.MemoireTheme
import com.umrhsn.mmoire.utils.EXTRA_BOARD_SIZE
import com.umrhsn.mmoire.utils.EXTRA_GAME_NAME
import com.umrhsn.mmoire.utils.explosionConfettiArray
import com.umrhsn.mmoire.utils.rainingConfettiLong
import com.umrhsn.mmoire.utils.rainingConfettiShort
import com.umrhsn.mmoire.utils.showToastSmoothWin
import com.umrhsn.mmoire.utils.showToastYouWon
import com.umrhsn.mmoire.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        viewModel.refreshSettings()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MemoireTheme(appTheme = uiState.appTheme) {
                MainScreen(
                    viewModel = viewModel,
                    appTheme = uiState.appTheme,
                    onCreateClicked = { desiredSize ->
                        val intent = Intent(this, CreateActivity::class.java).putExtra(
                            EXTRA_BOARD_SIZE,
                            desiredSize
                        )
                        resultLauncher.launch(intent)
                    },
                    onBrowseClicked = {
                        val intent = Intent(this, BrowseActivity::class.java)
                        resultLauncher.launch(intent)
                    },
                    onSettingsClicked = {
                        val intent = Intent(this, SettingsActivity::class.java)
                        startActivity(intent)
                    },
                    onCardClicked = { position, player -> viewModel.flipCard(position, player) },
                    onWin = { isSmoothWin -> triggerWinEffects(isSmoothWin) }
                )
            }
        }
    }

    private fun triggerWinEffects(isSmoothWin: Boolean) {
        val rootView = window.decorView.findViewById<ViewGroup>(R.id.content)
        if (isSmoothWin) {
            showToastSmoothWin(this)
            rainingConfettiLong(rootView)
            explosionConfettiArray(rootView)
        } else {
            showToastYouWon(this)
            rainingConfettiShort(rootView)
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val customGameName = data?.getStringExtra(EXTRA_GAME_NAME)
                if (customGameName != null) {
                    viewModel.downloadGame(customGameName)
                }
            }
        }
}
