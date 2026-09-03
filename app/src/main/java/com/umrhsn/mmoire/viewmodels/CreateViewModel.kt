package com.umrhsn.mmoire.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.repository.GameRepository
import com.umrhsn.mmoire.utils.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateUiState(
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val isSuccess: Boolean = false,
    val errorMessage: Int? = null,
    val errorArg: String? = null,
    val gameName: String? = null,
    val initialUris: List<Uri> = emptyList(),
    val nameTaken: Boolean = false
)

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val repository: GameRepository,
    private val soundManager: SoundManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUiState())
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

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

    fun createGame(gameName: String, imageByteArrays: List<ByteArray>, oldName: String? = null) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploading = true,
                    errorMessage = null,
                    errorArg = null,
                    nameTaken = false
                )
            }

            if (gameName != oldName && repository.checkGameExists(gameName)) {
                soundManager.playSound(SoundManager.SoundType.MATCH_FAIL)
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        nameTaken = true,
                        gameName = gameName
                    )
                }
                return@launch
            }

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
}
