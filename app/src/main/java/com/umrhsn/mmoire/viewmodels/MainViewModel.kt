package com.umrhsn.mmoire.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.models.MemoryGame
import com.umrhsn.mmoire.repository.GameRepository
import com.umrhsn.mmoire.utils.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val boardSize: BoardSize = BoardSize.SUPER_DUPER_EASY,
    val memoryGame: MemoryGame? = null,
    val gameName: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: Int? = null, // Store string resource ID
    val errorArg: String? = null,
    val customImages: List<String>? = null,
    val gameSessionId: Long = 0L
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: GameRepository,
    private val soundManager: SoundManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        setupBoard(_uiState.value.boardSize)
    }

    fun setupBoard(boardSize: BoardSize, customImages: List<String>? = null, gameName: String? = null) {
        val game = MemoryGame.create(boardSize, customImages)
        _uiState.update {
            it.copy(
                boardSize = boardSize,
                memoryGame = game,
                customImages = customImages,
                gameName = gameName,
                errorMessage = null,
                errorArg = null,
                isLoading = false,
                gameSessionId = System.currentTimeMillis()
            )
        }
    }

    fun flipCard(position: Int): Boolean {
        if (_uiState.value.isLoading) return false
        val game = _uiState.value.memoryGame ?: return false
        
        val isFirstCard = game.indexOfSingleSelectedCard == null
        val (updatedGame, foundMatch) = game.flipCard(position)
        
        if (isFirstCard) {
            soundManager.playSound(SoundManager.SoundType.CARD_FLIP)
        } else {
            if (foundMatch) {
                soundManager.playSound(SoundManager.SoundType.MATCH_SUCCESS)
            } else {
                soundManager.playSound(SoundManager.SoundType.MATCH_FAIL)
            }
        }

        _uiState.update {
            it.copy(memoryGame = updatedGame)
        }
        return foundMatch
    }

    fun downloadGame(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, errorArg = null) }
            val userImageList = repository.getGame(name)
            if (userImageList?.images != null) {
                val numCards = userImageList.images.size * 2
                val boardSize = BoardSize.getByValue(numCards)
                setupBoard(boardSize, userImageList.images, name)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = R.string.game_not_found,
                        errorArg = name
                    )
                }
            }
        }
    }

    fun playWinSound() {
        soundManager.playSound(SoundManager.SoundType.GAME_WIN)
    }

    fun refreshGame() {
        setupBoard(_uiState.value.boardSize, _uiState.value.customImages, _uiState.value.gameName)
    }
    
    fun changeSize(newSize: BoardSize) {
        setupBoard(newSize)
    }
}
