package com.example.iudigitalradio.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.iudigitalradio.domain.model.Country
import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.ui.components.StationCard
import com.example.iudigitalradio.ui.theme.CyanBright
import com.example.iudigitalradio.ui.theme.Glass
import com.example.iudigitalradio.ui.theme.VioletElectric

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onPlayStation: (RadioStation) -> Unit,
    onToggleFavorite: (RadioStation) -> Unit,
    favorites: List<RadioStation>,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = Glass.Level2,
                contentColor = CyanBright,
                indicator = { tabPositions ->
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[uiState.selectedTab])
                            .height(2.dp)
                            .background(CyanBright)
                    )
                }
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setTab(0) },
                    text = { Text("Buscar", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Filled.Search, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setTab(1) },
                    text = { Text("Explorar", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Filled.Public, contentDescription = null) }
                )
            }

            if (uiState.selectedTab == 0) {
                SearchTabContent(uiState, viewModel, favorites, onPlayStation, onToggleFavorite, onBack)
            } else {
                ExploreTabContent(uiState, viewModel, favorites, onPlayStation, onToggleFavorite)
            }
        }
    }
}

@Composable
fun SearchTabContent(
    uiState: SearchUiState,
    viewModel: SearchViewModel,
    favorites: List<RadioStation>,
    onPlayStation: (RadioStation) -> Unit,
    onToggleFavorite: (RadioStation) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Glass.Level1),
            placeholder = { Text("Buscar emisoras por nombre...", color = Color(0x66FFFFFF)) },
            leadingIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = CyanBright)
                }
            },
            trailingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = CyanBright) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanBright,
                unfocusedBorderColor = Glass.Border,
                cursorColor = CyanBright,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(Modifier.height(16.dp))

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = CyanBright)
            }
            uiState.query.isBlank() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Escribe para buscar emisoras", color = Color.White)
            }
            uiState.results.isEmpty() && !uiState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Sin resultados para \"${uiState.query}\"", color = Color.White)
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.results, key = { it.stationuuid }) { station ->
                    StationCard(
                        station = station.copy(isFavorite = favorites.any { it.stationuuid == station.stationuuid }),
                        isPlaying = false,
                        onPlay = { onPlayStation(station) },
                        onToggleFavorite = { onToggleFavorite(station) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreTabContent(
    uiState: SearchUiState,
    viewModel: SearchViewModel,
    favorites: List<RadioStation>,
    onPlayStation: (RadioStation) -> Unit,
    onToggleFavorite: (RadioStation) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        if (uiState.selectedCountry != null) {
            // Mostrar emisoras del país seleccionado
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectCountry(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a países", tint = CyanBright)
                }
                Text(
                    text = "Emisoras en ${uiState.selectedCountry.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VioletElectric
                )
            }
            
            if (uiState.isLoadingExplore) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = CyanBright) }
            } else if (uiState.countryStations.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No hay emisoras") }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.countryStations, key = { it.stationuuid }) { station ->
                        StationCard(
                            station = station.copy(isFavorite = favorites.any { it.stationuuid == station.stationuuid }),
                            isPlaying = false,
                            onPlay = { onPlayStation(station) },
                            onToggleFavorite = { onToggleFavorite(station) }
                        )
                    }
                }
            }
        } else {
            // Mostrar lista de continentes y países
            if (uiState.isLoadingExplore) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = CyanBright) }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                ) {
                    uiState.countriesByContinent.forEach { (continent, countries) ->
                        item {
                            Text(
                                text = continent,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = VioletElectric,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(countries) { country ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectCountry(country) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(country.name, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                Text("${country.stationCount} emisoras", color = Color.White.copy(alpha = 0.7f))
                            }
                            HorizontalDivider(color = Glass.Border)
                        }
                    }
                }
            }
        }
    }
}
