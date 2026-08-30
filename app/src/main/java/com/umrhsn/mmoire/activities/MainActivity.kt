package com.umrhsn.mmoire.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.umrhsn.mmoire.ui.screens.MainScreen
import com.umrhsn.mmoire.utils.*
import com.umrhsn.mmoire.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MainScreen(
                viewModel = viewModel,
                onCreateClicked = { desiredSize ->
                    val intent = Intent(this, CreateActivity::class.java).putExtra(EXTRA_BOARD_SIZE, desiredSize)
                    resultLauncher.launch(intent)
                },
                onBrowseClicked = {
                    val intent = Intent(this, BrowseActivity::class.java)
                    resultLauncher.launch(intent)
                },
                onCardClicked = { position -> viewModel.flipCard(position) },
                onWin = { isSmoothWin -> triggerWinEffects(isSmoothWin) }
            )
        }
    }

    private fun triggerWinEffects(isSmoothWin: Boolean) {
        val rootView = window.decorView.findViewById<android.view.ViewGroup>(android.R.id.content)
        if (isSmoothWin) {
            showToastSmoothWin(this)
            rainingConfettiLong(rootView)
            explosionConfettiArray(rootView)
        } else {
            showToastYouWon(this)
            rainingConfettiShort(rootView)
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val customGameName = data?.getStringExtra(EXTRA_GAME_NAME)
            if (customGameName != null) {
                viewModel.downloadGame(customGameName)
            }
        }
    }
}
