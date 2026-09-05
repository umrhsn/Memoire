package com.umrhsn.mmoire.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umrhsn.mmoire.models.AppTheme
import com.umrhsn.mmoire.repository.GameRepository
import com.umrhsn.mmoire.repository.UserImageListWithId
import com.umrhsn.mmoire.utils.PrefsManager
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
    val sortOrder: SortOrder = SortOrder.LATEST,
    val appTheme: AppTheme = AppTheme.SYSTEM
)

enum class SortOrder {
    LATEST, OLDEST, NAME_ASC, NAME_DESC, PAIRS_COUNT
}

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repository: GameRepository,
    private val soundManager: SoundManager,
    private val prefs: PrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseUiState(appTheme = prefs.getTheme()))
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        loadGames()

        // Observe theme changes globally
        viewModelScope.launch {
            prefs.themeFlow.collect { theme ->
                _uiState.update { it.copy(appTheme = theme) }
            }
        }
    }

    fun loadGames() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val games = repository.getAllLocalGames()
            _uiState.update { state ->
                val newState = state.copy(games = games, isLoading = false)
                newState.copy(filteredGames = getFilteredList(newState))
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val newState = state.copy(searchQuery = query)
            newState.copy(filteredGames = getFilteredList(newState))
        }
    }

    fun onSortOrderChanged(order: SortOrder) {
        _uiState.update { state ->
            val newState = state.copy(sortOrder = order)
            newState.copy(filteredGames = getFilteredList(newState))
        }
    }

    private fun getFilteredList(state: BrowseUiState): List<UserImageListWithId> {
        var result = state.games

        // Search
        if (state.searchQuery.isNotBlank()) {
            result = result.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
        }

        // Sort
        result = when (state.sortOrder) {
            SortOrder.LATEST -> result.sortedByDescending { it.createdAt }
            SortOrder.OLDEST -> result.sortedBy { it.createdAt }
            SortOrder.NAME_ASC -> result.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> result.sortedByDescending { it.name.lowercase() }
            SortOrder.PAIRS_COUNT -> result.sortedByDescending { it.images.size }
        }
        return result
    }

    fun deleteGame(gameName: String) {
        viewModelScope.launch {
            soundManager.playSound(SoundManager.SoundType.DELETE_ACTION)
            repository.deleteGame(gameName)
            loadGames()
        }
    }
}
