package com.umrhsn.mmoire.viewmodels

import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.db.RecordEntity
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.models.MemoryGame
import com.umrhsn.mmoire.repository.GameRepository
import com.umrhsn.mmoire.utils.DEFAULT_CARDS
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
import kotlin.time.Duration.Companion.seconds

data class MainUiState(
    val boardSize: BoardSize = BoardSize.SUPER_DUPER_EASY,
    val memoryGameP1: MemoryGame? = null,
    val memoryGameP2: MemoryGame? = null,
    val gameName: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: Int? = null,
    val errorArg: String? = null,
    val customImages: List<String>? = null,
    val gameSessionId: Long = 0L,
    val timerSeconds: Long = 0L,
    val timerSecondsP2: Long = 0L,
    val bestTime: Long? = null,
    val isTimerRunning: Boolean = false,
    val showTutorial: Boolean = false,
    val tutorialAnchors: Map<String, Rect> = emptyMap(),
    val appLanguage: String? = null,
    val isSoundEnabled: Boolean = true,
    val isTwoPlayerMode: Boolean = false,
    val winner: Int? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: GameRepository,
    private val soundManager: SoundManager,
    private val prefs: PrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUiState(
            appLanguage = prefs.getLanguage(),
            isSoundEnabled = prefs.isSoundEnabled()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var timerJobP1: Job? = null
    private var timerJobP2: Job? = null

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

    fun changeLanguage(lang: String?) {
        prefs.setLanguage(lang)
        _uiState.update { it.copy(appLanguage = lang) }
    }

    fun refreshSettings() {
        _uiState.update {
            it.copy(
                appLanguage = prefs.getLanguage(),
                isSoundEnabled = prefs.isSoundEnabled()
            )
        }
    }

    fun toggleSound(enabled: Boolean) {
        prefs.setSoundEnabled(enabled)
        _uiState.update { it.copy(isSoundEnabled = enabled) }
    }

    fun toggleTwoPlayerMode(enabled: Boolean) {
        _uiState.update { it.copy(isTwoPlayerMode = enabled) }
        refreshGame()
    }

    private fun startTimer(playerNumber: Int) {
        if (playerNumber == 1) {
            if (timerJobP1 != null) return
            timerJobP1 = viewModelScope.launch {
                while (true) {
                    delay(1.seconds)
                    _uiState.update { it.copy(timerSeconds = it.timerSeconds + 1) }
                }
            }
        } else {
            if (timerJobP2 != null) return
            timerJobP2 = viewModelScope.launch {
                while (true) {
                    delay(1.seconds)
                    _uiState.update { it.copy(timerSecondsP2 = it.timerSecondsP2 + 1) }
                }
            }
        }
    }

    private fun stopTimers() {
        timerJobP1?.cancel()
        timerJobP2?.cancel()
        timerJobP1 = null
        timerJobP2 = null
    }

    fun setupBoard(
        boardSize: BoardSize,
        customImages: List<String>? = null,
        gameName: String? = null
    ) {
        stopTimers()

        val numPairs = boardSize.getNumPairs()
        val sessionResources = if (customImages == null) {
            DEFAULT_CARDS.shuffled().take(numPairs)
        } else null

        val gameP1 = MemoryGame.create(boardSize, customImages, sessionResources)
        val gameP2 = if (_uiState.value.isTwoPlayerMode) {
            MemoryGame.create(boardSize, customImages, sessionResources)
        } else null

        val boardId = gameName ?: boardSize.name

        viewModelScope.launch {
            val record = repository.getRecord(boardId)
            _uiState.update {
                it.copy(
                    boardSize = boardSize,
                    memoryGameP1 = gameP1,
                    memoryGameP2 = gameP2,
                    customImages = customImages,
                    gameName = gameName,
                    errorMessage = null,
                    errorArg = null,
                    isLoading = false,
                    gameSessionId = System.currentTimeMillis(),
                    timerSeconds = 0,
                    timerSecondsP2 = 0,
                    bestTime = record?.bestTimeSeconds,
                    winner = null
                )
            }
        }
    }

    fun flipCard(position: Int, playerNumber: Int): Boolean {
        if (_uiState.value.isLoading) return false
        val state = _uiState.value
        val game = if (playerNumber == 1) state.memoryGameP1 else state.memoryGameP2
        if (game == null) return false

        if (game.numCardFlips == 0) {
            startTimer(playerNumber)
        }

        val isFirstCard = game.indexOfSingleSelectedCard == null
        val (updatedGame, foundMatch) = game.flipCard(position)

        if (isFirstCard) {
            if (_uiState.value.isSoundEnabled) soundManager.playSound(SoundManager.SoundType.CARD_FLIP)
        } else {
            if (_uiState.value.isSoundEnabled) {
                if (foundMatch) {
                    soundManager.playSound(SoundManager.SoundType.MATCH_SUCCESS)
                } else {
                    soundManager.playSound(SoundManager.SoundType.MATCH_FAIL)
                }
            }
        }

        _uiState.update { currentState ->
            if (playerNumber == 1) currentState.copy(memoryGameP1 = updatedGame)
            else currentState.copy(memoryGameP2 = updatedGame)
        }

        if (updatedGame.haveWonGame()) {
            handleWin(playerNumber)
        }

        return foundMatch
    }

    private fun handleWin(winnerPlayer: Int) {
        stopTimers()
        val state = _uiState.value
        val boardId = state.gameName ?: state.boardSize.name
        val currentTime = if (winnerPlayer == 1) state.timerSeconds else state.timerSecondsP2
        val currentMoves =
            (if (winnerPlayer == 1) state.memoryGameP1 else state.memoryGameP2)?.getNumMoves() ?: 0

        if (state.isTwoPlayerMode) {
            _uiState.update { it.copy(winner = winnerPlayer) }
            return
        }

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
        if (_uiState.value.isSoundEnabled) soundManager.playSound(SoundManager.SoundType.GAME_WIN)
    }

    fun playClickSound() {
        if (_uiState.value.isSoundEnabled) soundManager.playSound(SoundManager.SoundType.BUTTON_CLICK)
    }

    fun refreshGame() {
        setupBoard(_uiState.value.boardSize, _uiState.value.customImages, _uiState.value.gameName)
    }

    fun changeSize(newSize: BoardSize) {
        setupBoard(newSize)
    }
}
