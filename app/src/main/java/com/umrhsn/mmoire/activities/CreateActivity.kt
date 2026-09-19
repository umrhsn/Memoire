package com.umrhsn.mmoire.activities

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.ui.components.AppDialog
import com.umrhsn.mmoire.ui.screens.CreateScreen
import com.umrhsn.mmoire.ui.theme.MemoireTheme
import com.umrhsn.mmoire.utils.EXTRA_BOARD_SIZE
import com.umrhsn.mmoire.utils.EXTRA_EDIT_GAME_NAME
import com.umrhsn.mmoire.utils.PickMultipleVisualMediaWithLimit
import com.umrhsn.mmoire.utils.isPermissionGranted
import com.umrhsn.mmoire.viewmodels.CreateViewModel
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.AlertTriangle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateActivity : ComponentActivity() {

    private val viewModel: CreateViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    private var boardSize by mutableStateOf<BoardSize?>(null)
    private val chosenImageUris = mutableStateListOf<Uri>()
    private var replaceIndex: Int = -1
    private var oldGameName: String? = null

    private val multiplePhotoPickerLauncher =
        registerForActivityResult(PickMultipleVisualMediaWithLimit()) { uris ->
            val numImagesRequired = boardSize?.getNumPairs() ?: 0
            uris.forEach { uri ->
                if (chosenImageUris.size < numImagesRequired && !chosenImageUris.contains(uri)) {
                    chosenImageUris.add(uri)
                }
            }
        }

    private val singlePhotoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                if (replaceIndex != -1 && replaceIndex < chosenImageUris.size) {
                    chosenImageUris[replaceIndex] = uri
                } else if (replaceIndex == -1) {
                    val numImagesRequired = boardSize?.getNumPairs() ?: 0
                    if (chosenImageUris.size < numImagesRequired && !chosenImageUris.contains(uri)) {
                        chosenImageUris.add(uri)
                    }
                }
            }
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                launchPhotoPicker(replaceIndex)
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG)
                    .show()
            }
        }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        oldGameName = intent.getStringExtra(EXTRA_EDIT_GAME_NAME)
        boardSize =
            IntentCompat.getSerializableExtra(intent, EXTRA_BOARD_SIZE, BoardSize::class.java)

        if (oldGameName != null) {
            viewModel.loadGame(oldGameName!!)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val windowSizeClass = calculateWindowSizeClass(this)

            MemoireTheme(
                appTheme = uiState.appTheme,
                appColorTheme = uiState.appColorTheme,
                isTintEnabled = uiState.isTintEnabled
            ) {
                // Sync local state with loaded game
                LaunchedEffect(uiState.initialUris) {
                    if (uiState.initialUris.isNotEmpty() && chosenImageUris.isEmpty()) {
                        chosenImageUris.addAll(uiState.initialUris)
                        val numCards = uiState.initialUris.size * 2
                        boardSize = BoardSize.getByValue(numCards)
                    }
                }

                if (uiState.isLoading || (oldGameName != null && boardSize == null && uiState.errorMessage == null)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    return@MemoireTheme
                }

                if (uiState.errorMessage != null && boardSize == null) {
                    AppDialog(
                        onDismissRequest = { finish() },
                        title = stringResource(R.string.error),
                        icon = EvaIcons.Outline.AlertTriangle
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(
                                    uiState.errorMessage!!,
                                    uiState.errorArg ?: ""
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { finish() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(stringResource(R.string.back), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    return@MemoireTheme
                }

                if (boardSize == null && oldGameName == null) {
                    // Should not happen with proper navigation
                    finish()
                    return@MemoireTheme
                }

                if (boardSize != null) {
                    CreateScreen(
                        viewModel = viewModel,
                        windowSizeClass = windowSizeClass,
                        boardSize = boardSize!!,
                        chosenImageUris = chosenImageUris,
                        oldName = oldGameName,
                        onBackClicked = { finish() },
                        onPlaceholderClicked = { handlePlaceholderClick(-1) },
                        onImageClicked = { index -> handlePlaceholderClick(index) },
                        onRemoveImage = { uri -> chosenImageUris.remove(uri) },
                        onSaveClicked = { gameName -> handleSaveClick(gameName) }
                    )
                }
            }
        }
    }

    private fun handlePlaceholderClick(index: Int) {
        replaceIndex = index
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (Build.VERSION.SDK_INT >= 34) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
            } else {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launchPhotoPicker(index)
        } else {
            if (permissions.all { isPermissionGranted(this, it) }) {
                launchPhotoPicker(index)
            } else {
                permissionLauncher.launch(permissions)
            }
        }
    }

    private fun launchPhotoPicker(index: Int) {
        val numImagesRequired = boardSize?.getNumPairs() ?: 0
        val pickerLimit = if (index != -1) 1 else (numImagesRequired - chosenImageUris.size)

        if (pickerLimit <= 0) {
            Toast.makeText(this, getString(R.string.board_full), Toast.LENGTH_SHORT).show()
            return
        }

        if (index != -1 || pickerLimit == 1) {
            singlePhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            multiplePhotoPickerLauncher.launch(
                PickMultipleVisualMediaWithLimit.Request(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    pickerLimit
                )
            )
        }
    }

    private fun handleSaveClick(gameName: String) {
        viewModel.createGame(contentResolver, gameName, chosenImageUris, oldGameName)
    }
}
