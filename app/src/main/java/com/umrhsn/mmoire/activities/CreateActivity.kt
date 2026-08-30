package com.umrhsn.mmoire.activities

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.IntentCompat
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.networking.BitmapScaler
import com.umrhsn.mmoire.ui.screens.CreateScreen
import com.umrhsn.mmoire.utils.EXTRA_BOARD_SIZE
import com.umrhsn.mmoire.utils.isPermissionGranted
import com.umrhsn.mmoire.viewmodels.CreateViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class CreateActivity : AppCompatActivity() {

    private val viewModel: CreateViewModel by viewModels()
    private lateinit var boardSize: BoardSize
    private var numImagesRequired: Int = -1
    private val chosenImageUris = mutableStateListOf<Uri>()
    private var replaceIndex: Int = -1

    // Standard activity result launcher to handle pickers with dynamic limits
    private val photoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data!!
            val clipData = data.clipData
            val selectedUri = data.data

            if (replaceIndex != -1) {
                // Replacing a specific image
                val newUri = if (clipData != null && clipData.itemCount > 0) {
                    clipData.getItemAt(0).uri
                } else {
                    selectedUri
                }
                
                if (newUri != null && replaceIndex < chosenImageUris.size) {
                    chosenImageUris[replaceIndex] = newUri
                }
            } else {
                // Adding new images
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        if (chosenImageUris.size < numImagesRequired && !chosenImageUris.contains(uri)) {
                            chosenImageUris.add(uri)
                        }
                    }
                } else if (selectedUri != null) {
                    if (chosenImageUris.size < numImagesRequired && !chosenImageUris.contains(selectedUri)) {
                        chosenImageUris.add(selectedUri)
                    }
                }
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            launchPhotoPicker(replaceIndex)
        } else {
            Toast.makeText(this, "Permission denied. Access to photos is required.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        boardSize = IntentCompat.getSerializableExtra(intent, EXTRA_BOARD_SIZE, BoardSize::class.java)!!
        numImagesRequired = boardSize.getNumPairs()

        setContent {
            CreateScreen(
                viewModel = viewModel,
                boardSize = boardSize,
                chosenImageUris = chosenImageUris,
                onBackClicked = { finish() },
                onPlaceholderClicked = { handlePlaceholderClick(-1) },
                onImageClicked = { index -> handlePlaceholderClick(index) },
                onRemoveImage = { uri -> chosenImageUris.remove(uri) },
                onSaveClicked = { gameName -> handleSaveClick(gameName) }
            )
        }
    }

    private fun handlePlaceholderClick(index: Int) {
        replaceIndex = index
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (Build.VERSION.SDK_INT >= 34) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
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
        
        if (pickerLimit <= 0 && index == -1) {
            Toast.makeText(this, "Board is already full", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Modern Photo Picker (API 33+)
            Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = "image/*"
                if (pickerLimit > 1) {
                    putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, pickerLimit)
                }
                // Force numeric ordering badges in the native gallery selection (Android 14+)
                if (Build.VERSION.SDK_INT >= 34 && pickerLimit > 1) {
                    putExtra("android.provider.extra.PICK_IMAGES_IN_ORDER", true)
                }
            }
        } else {
            // Legacy Picker
            Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
                if (pickerLimit > 1) {
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
            }
        }
        
        try {
            photoPickerLauncher.launch(intent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                if (pickerLimit > 1) {
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
            }
            photoPickerLauncher.launch(fallbackIntent)
        }
    }

    private fun handleSaveClick(gameName: String) {
        val byteArrays = chosenImageUris.map { getImageByteArray(it) }
        viewModel.createGame(gameName, byteArrays)
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
            // Fallback to a tiny empty bitmap if loading fails to prevent crash
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()
    }
}
