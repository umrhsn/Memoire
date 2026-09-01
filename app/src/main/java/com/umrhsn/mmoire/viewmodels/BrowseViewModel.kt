package com.umrhsn.mmoire.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umrhsn.mmoire.repository.GameRepository
import com.umrhsn.mmoire.repository.UserImageListWithId
import com.umrhsn.mmoire.utils.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrowseUiState(
    val games: List<UserImageListWithId> = emptyList(),
    val filteredGames: List<UserImageListWithId> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.LATEST
)

enum class SortOrder {
    LATEST, OLDEST, NAME_ASC, NAME_DESC, PAIRS_COUNT
}

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repository: GameRepository,
    private val soundManager: SoundManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    fun loadGames() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val games = repository.getAllLocalGames()
            _uiState.update { it.copy(games = games, isLoading = false) }
            applyFilters()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onSortOrderChanged(order: SortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        var result = currentState.games

        // Search
        if (currentState.searchQuery.isNotBlank()) {
            result = result.filter { it.name.contains(currentState.searchQuery, ignoreCase = true) }
        }

        // Sort
        result = when (currentState.sortOrder) {
            SortOrder.LATEST -> result.sortedByDescending { it.createdAt }
            SortOrder.OLDEST -> result.sortedBy { it.createdAt }
            SortOrder.NAME_ASC -> result.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> result.sortedByDescending { it.name.lowercase() }
            SortOrder.PAIRS_COUNT -> result.sortedByDescending { it.images.size }
        }

        _uiState.update { it.copy(filteredGames = result) }
    }

    fun playButtonClick() {
        soundManager.playSound(SoundManager.SoundType.BUTTON_CLICK)
    }

    fun deleteGame(gameName: String) {
        viewModelScope.launch {
            soundManager.playSound(SoundManager.SoundType.DELETE_ACTION)
            repository.deleteGame(gameName)
            loadGames()
        }
    }
}
