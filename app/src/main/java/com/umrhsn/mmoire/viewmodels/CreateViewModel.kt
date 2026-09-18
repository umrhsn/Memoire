package com.umrhsn.mmoire.viewmodels

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.AppColorTheme
import com.umrhsn.mmoire.models.AppTheme
import com.umrhsn.mmoire.networking.BitmapScaler
import com.umrhsn.mmoire.repository.GameRepository
import com.umrhsn.mmoire.utils.PrefsManager
import com.umrhsn.mmoire.utils.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class CreateUiState(
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val isProcessing: Boolean = false,
    val uploadProgress: Int = 0,
    val isSuccess: Boolean = false,
    val errorMessage: Int? = null,
    val errorArg: String? = null,
    val gameName: String? = null,
    val initialUris: List<Uri> = emptyList(),
    val nameTaken: Boolean = false,
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val appColorTheme: AppColorTheme = AppColorTheme.DEFAULT,
    val isTintEnabled: Boolean = false,
    val appLanguage: String? = null
)

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val repository: GameRepository,
    private val soundManager: SoundManager,
    private val prefs: PrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreateUiState(
            appTheme = prefs.getTheme(),
            appColorTheme = prefs.getColorTheme(),
            isTintEnabled = prefs.isBackgroundTintEnabled(),
            appLanguage = prefs.getLanguage()
        )
    )
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    init {
        // Observe theme and locale changes globally
        viewModelScope.launch {
            prefs.themeFlow.collect { theme ->
                _uiState.update { it.copy(appTheme = theme) }
            }
        }
        viewModelScope.launch {
            prefs.colorThemeFlow.collect { colorTheme ->
                _uiState.update { it.copy(appColorTheme = colorTheme) }
            }
        }
        viewModelScope.launch {
            prefs.tintFlow.collect { enabled ->
                _uiState.update { it.copy(isTintEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            prefs.localeFlow.collect { lang ->
                _uiState.update { it.copy(appLanguage = lang) }
            }
        }
    }

    fun resetState() {
        _uiState.update { CreateUiState() }
    }

    fun playButtonClick() {
        soundManager.playSound(SoundManager.SoundType.BUTTON_CLICK)
    }

    fun loadGame(gameName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userImageList = repository.getGame(gameName)
            if (userImageList != null) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        gameName = gameName,
                        initialUris = userImageList.images?.map { Uri.parse(it) } ?: emptyList()
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun createGame(
        contentResolver: ContentResolver,
        gameName: String,
        imageUris: List<Uri>,
        oldName: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    isUploading = false,
                    errorMessage = null,
                    errorArg = null,
                    nameTaken = false
                )
            }

            if (gameName != oldName && repository.checkGameExists(gameName)) {
                soundManager.playSound(SoundManager.SoundType.MATCH_FAIL)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        nameTaken = true,
                        gameName = gameName
                    )
                }
                return@launch
            }

            // Process images on IO dispatcher to avoid ANR
            val imageByteArrays = withContext(Dispatchers.IO) {
                imageUris.map { uri ->
                    getImageByteArray(contentResolver, uri)
                }
            }

            _uiState.update { it.copy(isProcessing = false, isUploading = true) }

            val imageUrls = mutableListOf<String>()

            imageByteArrays.forEachIndexed { index, bytes ->
                try {
                    val url = repository.uploadImage(gameName, index, bytes)
                    imageUrls.add(url)
                    _uiState.update {
                        it.copy(
                            uploadProgress = (imageUrls.size * 100) / imageByteArrays.size
                        )
                    }
                } catch (e: Exception) {
                    soundManager.playSound(SoundManager.SoundType.MATCH_FAIL)
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            errorMessage = R.string.failed_to_save,
                            errorArg = e.localizedMessage
                        )
                    }
                    return@launch
                }
            }

            val success = if (oldName != null) {
                repository.updateGame(oldName, gameName, imageUrls)
            } else {
                repository.createGame(gameName, imageUrls)
            }

            if (success) {
                soundManager.playSound(SoundManager.SoundType.SUCCESS_FANFARE)
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        isSuccess = true,
                        gameName = gameName
                    )
                }
            } else {
                soundManager.playSound(SoundManager.SoundType.MATCH_FAIL)
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = R.string.failed_to_create_db
                    )
                }
            }
        }
    }

    private fun getImageByteArray(contentResolver: ContentResolver, photoUri: Uri): ByteArray {
        val originalBitmap = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
            }
        } catch (e: Exception) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()
    }
}
