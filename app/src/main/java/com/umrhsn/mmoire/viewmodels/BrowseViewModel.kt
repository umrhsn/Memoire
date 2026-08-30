package com.umrhsn.mmoire.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umrhsn.mmoire.repository.GameRepository
import com.umrhsn.mmoire.repository.UserImageListWithId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrowseUiState(
    val games: List<UserImageListWithId> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState

    init {
        loadGames()
    }

    fun loadGames() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val games = repository.getAllLocalGames()
            _uiState.value = _uiState.value.copy(games = games, isLoading = false)
        }
    }

    fun deleteGame(gameName: String) {
        viewModelScope.launch {
            repository.deleteGame(gameName)
            loadGames()
        }
    }
}
