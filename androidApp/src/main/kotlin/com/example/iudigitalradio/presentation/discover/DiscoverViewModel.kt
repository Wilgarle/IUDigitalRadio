package com.example.iudigitalradio.presentation.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.domain.usecase.GetStationsByCountryUseCase
import com.example.iudigitalradio.domain.usecase.GetTopStationsUseCase
import com.example.iudigitalradio.domain.usecase.SearchStationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val stations: List<RadioStation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedGenre: String? = null,
    val selectedCountry: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val getTopStationsUseCase: GetTopStationsUseCase,
    private val searchStationsUseCase: SearchStationsUseCase,
    private val getStationsByCountryUseCase: GetStationsByCountryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    /** Flow del query de búsqueda — debounce(300ms) aplicado en la recopilación */
    private val _searchQuery = MutableStateFlow("")

    init {
        loadTopStations()
        observeSearchQuery()
    }

    fun loadTopStations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Carga Colombia primero y completa con top global
            val colombiaResult = getStationsByCountryUseCase("CO")
            val colombiaStations = colombiaResult.getOrElse { emptyList() }.take(20)

            getTopStationsUseCase()
                .onSuccess { globalStations ->
                    // Combina: Colombia primero, luego globales sin duplicados
                    val colombiaUuids = colombiaStations.map { it.stationuuid }.toSet()
                    val fill = globalStations.filter { it.stationuuid !in colombiaUuids }.take(80)
                    _uiState.update { it.copy(stations = colombiaStations + fill, isLoading = false) }
                }
                .onFailure { err ->
                    // Si falla lo global, al menos muestra las de Colombia
                    if (colombiaStations.isNotEmpty()) {
                        _uiState.update { it.copy(stations = colombiaStations, isLoading = false) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = err.message) }
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /** CONSTRAINT: debounce(300ms) para no saturar la API */
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300L)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        loadTopStations()
                    } else {
                        _uiState.update { it.copy(isLoading = true) }
                        searchStationsUseCase(query)
                            .onSuccess { stations ->
                                _uiState.update { it.copy(stations = stations, isLoading = false) }
                            }
                            .onFailure { err ->
                                _uiState.update { it.copy(isLoading = false, error = err.message) }
                            }
                    }
                }
        }
    }

    fun loadByCountry(countryCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedCountry = countryCode) }
            getStationsByCountryUseCase(countryCode)
                .onSuccess { stations ->
                    _uiState.update { it.copy(stations = stations, isLoading = false) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                }
        }
    }
}
