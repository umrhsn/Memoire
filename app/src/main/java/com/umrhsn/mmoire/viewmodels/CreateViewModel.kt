package com.umrhsn.mmoire.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateUiState(
    val isUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val isSuccess: Boolean = false,
    val errorMessage: Int? = null, // String resource ID
    val errorArg: String? = null,
    val gameName: String? = null,
    val nameTaken: Boolean = false
)

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUiState())
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.update { CreateUiState() }
    }

    fun createGame(gameName: String, imageByteArrays: List<ByteArray>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null, errorArg = null, nameTaken = false) }
            
            // Check if name is taken
            if (repository.checkGameExists(gameName)) {
                _uiState.update { it.copy(isUploading = false, nameTaken = true, gameName = gameName) }
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

            val success = repository.createGame(gameName, imageUrls)
            if (success) {
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        isSuccess = true,
                        gameName = gameName
                    )
                }
            } else {
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
