package com.example.iudigitalradio.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.domain.usecase.GetFavoriteStationsUseCase
import com.example.iudigitalradio.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<RadioStation> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    getFavoritesUseCase: GetFavoriteStationsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getFavoritesUseCase().collect { favs ->
                _uiState.update { it.copy(favorites = favs, isLoading = false) }
            }
        }
    }

    fun removeFavorite(station: RadioStation) {
        viewModelScope.launch { toggleFavoriteUseCase(station) }
    }
}
