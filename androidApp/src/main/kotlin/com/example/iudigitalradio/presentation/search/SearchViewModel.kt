package com.example.iudigitalradio.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.domain.model.Country
import com.example.iudigitalradio.domain.usecase.GetCountriesUseCase
import com.example.iudigitalradio.domain.usecase.GetStationsByCountryUseCase
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

data class SearchUiState(
    val results: List<RadioStation> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSearchActive: Boolean = false,
    val selectedTab: Int = 0,
    val countriesByContinent: Map<String, List<Country>> = emptyMap(),
    val countryStations: List<RadioStation> = emptyList(),
    val selectedCountry: Country? = null,
    val isLoadingExplore: Boolean = false
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchStationsUseCase: SearchStationsUseCase,
    private val getCountriesUseCase: GetCountriesUseCase,
    private val getStationsByCountryUseCase: GetStationsByCountryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _queryFlow
                .debounce(300L)   // CONSTRAINT: debounce 300ms
                .distinctUntilChanged()
                .collect { query ->
                    if (query.length >= 2) {
                        performSearch(query)
                    } else if (query.isBlank()) {
                        _uiState.update { it.copy(results = emptyList()) }
                    }
                }
        }
        
        loadCountries()
    }

    private fun loadCountries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingExplore = true) }
            getCountriesUseCase()
                .onSuccess { list ->
                    val grouped = list
                        .filter { it.isoCode.isNotBlank() && it.stationCount > 0 }
                        .groupBy { getContinent(it.isoCode) }
                        .toSortedMap()
                    _uiState.update { it.copy(countriesByContinent = grouped, isLoadingExplore = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingExplore = false) }
                }
        }
    }

    private fun getContinent(isoCode: String): String {
        val am = setOf("US","CA","MX","CO","AR","BR","CL","PE","VE","EC","UY","PY","BO","CU","DO","GT","HN","SV","NI","CR","PA")
        val eu = setOf("GB","FR","DE","IT","ES","RU","UA","PL","RO","NL","BE","SE","NO","DK","FI","CH","AT","PT","GR","IE")
        val as_ = setOf("CN","IN","JP","KR","ID","PK","BD","PH","VN","TR","IR","TH","MM","IQ","SA","UZ","MY","YE","NP","LK")
        val af = setOf("NG","ET","EG","CD","ZA","TZ","KE","UG","DZ","SD","MA","AO","MZ","GH","MG","CM","CI","NE","BF","ML")
        val oc = setOf("AU","NZ","PG","FJ","SB","VU","WS","KI","TO","FM","PW","MH","TV","NR")
        val code = isoCode.uppercase()
        return when {
            am.contains(code) -> "América"
            eu.contains(code) -> "Europa"
            as_.contains(code) -> "Asia"
            af.contains(code) -> "África"
            oc.contains(code) -> "Oceanía"
            else -> "Otros"
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        _queryFlow.value = query
    }

    fun setSearchActive(active: Boolean) {
        _uiState.update { it.copy(isSearchActive = active) }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            searchStationsUseCase(query)
                .onSuccess { stations ->
                    _uiState.update { it.copy(results = stations, isLoading = false) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                }
        }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index, isSearchActive = false) }
    }

    fun selectCountry(country: Country?) {
        _uiState.update { it.copy(selectedCountry = country) }
        if (country != null) {
            loadCountryStations(country.isoCode)
        } else {
            _uiState.update { it.copy(countryStations = emptyList()) }
        }
    }

    private fun loadCountryStations(isoCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingExplore = true) }
            getStationsByCountryUseCase(isoCode)
                .onSuccess { stations ->
                    _uiState.update { it.copy(countryStations = stations, isLoadingExplore = false) }
                }
                .onFailure { 
                    _uiState.update { it.copy(isLoadingExplore = false) }
                }
        }
    }
}
