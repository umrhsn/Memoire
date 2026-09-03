package com.umrhsn.mmoire.activities

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.IntentCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.networking.BitmapScaler
import com.umrhsn.mmoire.ui.screens.CreateScreen
import com.umrhsn.mmoire.utils.EXTRA_BOARD_SIZE
import com.umrhsn.mmoire.utils.EXTRA_EDIT_GAME_NAME
import com.umrhsn.mmoire.utils.isPermissionGranted
import com.umrhsn.mmoire.viewmodels.CreateViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class CreateActivity : ComponentActivity() {

    private val viewModel: CreateViewModel by viewModels()
    private var boardSize: BoardSize? = null
    private var numImagesRequired: Int = -1
    private val chosenImageUris = mutableStateListOf<Uri>()
    private var replaceIndex: Int = -1
    private var oldGameName: String? = null

    private val multiplePhotoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(20)) { uris ->
            uris.forEach { uri ->
                if (chosenImageUris.size < numImagesRequired && !chosenImageUris.contains(uri)) {
                    chosenImageUris.add(uri)
                }
            }
        }

    private val singlePhotoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null && replaceIndex != -1 && replaceIndex < chosenImageUris.size) {
                chosenImageUris[replaceIndex] = uri
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

            // Sync local state with loaded game
            LaunchedEffect(uiState.initialUris) {
                if (uiState.initialUris.isNotEmpty() && chosenImageUris.isEmpty()) {
                    chosenImageUris.addAll(uiState.initialUris)
                    val numCards = uiState.initialUris.size * 2
                    boardSize = BoardSize.getByValue(numCards)
                    numImagesRequired = boardSize!!.getNumPairs()
                }
            }

            if (boardSize == null && oldGameName == null) {
                // Should not happen with proper navigation
                finish()
                return@setContent
            }

            if (boardSize != null) {
                numImagesRequired = boardSize!!.getNumPairs()
            }

            if (boardSize != null) {
                CreateScreen(
                    viewModel = viewModel,
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
        val pickerLimit = if (index != -1) 1 else (numImagesRequired - chosenImageUris.size)

        if (pickerLimit <= 0) {
            Toast.makeText(this, getString(R.string.board_full), Toast.LENGTH_SHORT).show()
            return
        }

        if (index != -1) {
            singlePhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            multiplePhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun handleSaveClick(gameName: String) {
        // If we are editing, we might have mixed local Uris and raw Bitmaps if we pick new ones.
        // For simplicity, we'll re-process all Uris into byte arrays.
        val byteArrays = chosenImageUris.map { getImageByteArray(it) }
        viewModel.createGame(gameName, byteArrays, oldGameName)
    }

    private fun getImageByteArray(photoUri: Uri): ByteArray {
        val originalBitmap = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
            }
        } catch (e: Exception) {
            createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()
    }
}
