package com.umrhsn.mmoire.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.umrhsn.mmoire.ui.screens.BrowseScreen
import com.umrhsn.mmoire.utils.EXTRA_GAME_NAME
import com.umrhsn.mmoire.viewmodels.BrowseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BrowseActivity : AppCompatActivity() {

    private val viewModel: BrowseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            BrowseScreen(
                viewModel = viewModel,
                onBackClicked = { finish() },
                onGameSelected = { gameName ->
                    val resultData = Intent()
                    resultData.putExtra(EXTRA_GAME_NAME, gameName)
                    setResult(RESULT_OK, resultData)
                    finish()
                }
            )
        }
    }
}
