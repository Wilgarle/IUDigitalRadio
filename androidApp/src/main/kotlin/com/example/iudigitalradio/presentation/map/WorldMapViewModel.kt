package com.example.iudigitalradio.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.domain.repository.RadioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorldMapUiState(
    val stations: List<RadioStation> = emptyList(),
    val isLoading: Boolean = false,
    val selectedStation: RadioStation? = null,
    val error: String? = null
)

/** Códigos ISO-3166-1 Alpha-2 de países latinoamericanos/americanos clave */
private val AMERICA_COUNTRY_CODES = listOf("US", "MX", "AR", "BR", "CL", "PE", "EC", "VE", "BO", "PY", "UY", "CR", "PA", "DO", "CU")

@HiltViewModel
class WorldMapViewModel @Inject constructor(
    private val repository: RadioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorldMapUiState())
    val uiState: StateFlow<WorldMapUiState> = _uiState.asStateFlow()

    init {
        loadStationsForMap()
    }

    fun loadStationsForMap() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Peticiones en paralelo: Colombia (20), América (50 por país), Global (130)
                val colombiaDeferred = async { repository.getStationsByCountryForMap("CO", 20) }
                val americaDeferred  = async { repository.getStationsByCountriesForMap(AMERICA_COUNTRY_CODES, 50) }
                val globalDeferred   = async { repository.getStationsForMap(130) }

                val colombia = colombiaDeferred.await().getOrElse { emptyList() }
                val america  = americaDeferred.await().getOrElse  { emptyList() }
                val global   = globalDeferred.await().getOrElse   { emptyList() }

                // Unir con prioridad: Colombia primero, América después, Resto del mundo
                val seenUuids = mutableSetOf<String>()
                val merged = mutableListOf<RadioStation>()
                for (station in (colombia + america + global)) {
                    if (station.stationuuid !in seenUuids && station.hasCoordinates) {
                        seenUuids.add(station.stationuuid)
                        merged.add(station)
                    }
                }

                _uiState.update { it.copy(stations = merged, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectStation(station: RadioStation?) {
        _uiState.update { it.copy(selectedStation = station) }
    }
}

