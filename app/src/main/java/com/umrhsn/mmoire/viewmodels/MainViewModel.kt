package com.umrhsn.mmoire.viewmodels

import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.db.RecordEntity
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.models.MemoryGame
import com.umrhsn.mmoire.repository.GameRepository
import com.umrhsn.mmoire.utils.PrefsManager
import com.umrhsn.mmoire.utils.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val errorMessage: Int? = null,
    val errorArg: String? = null,
    val customImages: List<String>? = null,
    val gameSessionId: Long = 0L,
    val timerSeconds: Long = 0L,
    val bestTime: Long? = null,
    val isTimerRunning: Boolean = false,
    val showTutorial: Boolean = false,
    val tutorialAnchors: Map<String, Rect> = emptyMap()
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: GameRepository,
    private val soundManager: SoundManager,
    private val prefs: PrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        setupBoard(_uiState.value.boardSize)
        if (prefs.isFirstTime()) {
            _uiState.update { it.copy(showTutorial = true) }
        }
    }

    fun onAnchorPositioned(key: String, rect: Rect) {
        _uiState.update {
            it.copy(tutorialAnchors = it.tutorialAnchors + (key to rect))
        }
    }

    fun startTutorial() {
        _uiState.update { it.copy(showTutorial = true) }
    }

    fun dismissTutorial() {
        prefs.setFirstTime(false)
        _uiState.update { it.copy(showTutorial = false) }
    }

    private fun startTimer() {
        if (_uiState.value.isTimerRunning) return
        _uiState.update { it.copy(isTimerRunning = true) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(timerSeconds = it.timerSeconds + 1) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    fun setupBoard(
        boardSize: BoardSize,
        customImages: List<String>? = null,
        gameName: String? = null
    ) {
        stopTimer()
        val game = MemoryGame.create(boardSize, customImages)
        val boardId = gameName ?: boardSize.name

        viewModelScope.launch {
            val record = repository.getRecord(boardId)
            _uiState.update {
                it.copy(
                    boardSize = boardSize,
                    memoryGame = game,
                    customImages = customImages,
                    gameName = gameName,
                    errorMessage = null,
                    errorArg = null,
                    isLoading = false,
                    gameSessionId = System.currentTimeMillis(),
                    timerSeconds = 0,
                    bestTime = record?.bestTimeSeconds
                )
            }
        }
    }

    fun flipCard(position: Int): Boolean {
        if (_uiState.value.isLoading) return false
        val game = _uiState.value.memoryGame ?: return false

        // Start timer on first flip
        if (game.numCardFlips == 0) {
            startTimer()
        }

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

        if (updatedGame.haveWonGame()) {
            handleWin()
        }

        return foundMatch
    }

    private fun handleWin() {
        stopTimer()
        val boardId = _uiState.value.gameName ?: _uiState.value.boardSize.name
        val currentTime = _uiState.value.timerSeconds
        val currentMoves = _uiState.value.memoryGame?.getNumMoves() ?: 0

        viewModelScope.launch {
            val oldRecord = repository.getRecord(boardId)
            if (oldRecord == null || currentTime < oldRecord.bestTimeSeconds) {
                repository.saveRecord(RecordEntity(boardId, currentTime, currentMoves))
                _uiState.update { it.copy(bestTime = currentTime) }
            }
        }
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

    fun playClickSound() {
        soundManager.playSound(SoundManager.SoundType.BUTTON_CLICK)
    }

    fun refreshGame() {
        setupBoard(_uiState.value.boardSize, _uiState.value.customImages, _uiState.value.gameName)
    }

    fun changeSize(newSize: BoardSize) {
        setupBoard(newSize)
    }
}
