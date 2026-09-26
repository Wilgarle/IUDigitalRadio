package com.example.iudigitalradio.presentation.discover

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.presentation.player.RadioUiState
import com.example.iudigitalradio.ui.components.PlayerCard
import com.example.iudigitalradio.ui.components.StationCard
import com.example.iudigitalradio.ui.theme.CyanBright
import com.example.iudigitalradio.ui.theme.VioletElectric

/**
 * RF-01: Pantalla principal — Jetpack Compose pura, cero XML.
 * RF-06: LazyColumn de emisoras con animación slideInVertically.
 * RF-04: rememberSaveable preserva estado de scroll ante rotaciones.
 */
@Composable
fun DiscoverScreen(
    uiState: RadioUiState,
    onPlayStation: (RadioStation) -> Unit,
    onTogglePlay: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleFavorite: (RadioStation) -> Unit,
    onNextStation: () -> Unit,
    onPrevStation: () -> Unit,
    onSearch: (String) -> Unit,
    onSetVolume: (Float) -> Unit,
    onSetUserPhoto: (Bitmap) -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    modifier: Modifier = Modifier
) {
    // RF-04: rememberSaveable — preserva el estado de visibilidad del player en rotaciones
    var playerExpanded by rememberSaveable { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ── PlayerCard (RF-07) ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible = playerExpanded,
            enter   = slideInVertically(initialOffsetY = { -it }),
            exit    = slideOutVertically(targetOffsetY  = { -it })
        ) {
            PlayerCard(
                station         = uiState.selectedStation,
                isPlaying       = uiState.isPlaying,
                isMuted         = uiState.isMuted,
                isFavorite      = uiState.favorites.any { it.stationuuid == uiState.selectedStation?.stationuuid },
                volume          = uiState.volume,
                onPlayPause     = onTogglePlay,
                onNext          = onNextStation,
                onPrevious      = onPrevStation,
                onMute          = onToggleMute,
                onToggleFavorite = { uiState.selectedStation?.let { onToggleFavorite(it) } },
                onVolumeChange  = onSetVolume,
                modifier        = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // ── RF-06: LazyColumn de emisoras ─────────────────────────────────────
        if (uiState.isLoading && uiState.stations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyanBright)
            }
        } else if (uiState.error != null && uiState.stations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text  = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    // Sección Colombia
                    Text(
                        text  = "🇨🇴 Colombia",
                        style = MaterialTheme.typography.titleSmall,
                        color = CyanBright,
                        modifier = Modifier.padding(bottom = 2.dp, top = 4.dp)
                    )
                }

                item {
                    // Sección global (aparece cuando ya no son emisoras de Colombia)
                    Text(
                        text  = "🌍 Emisoras Populares",
                        style = MaterialTheme.typography.titleMedium,
                        color = VioletElectric,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // RF-06: Items renderizados con animación de entrada
                itemsIndexed(
                    items = uiState.stations,
                    key   = { _, station -> station.stationuuid }
                ) { index, station ->
                    AnimatedVisibility(
                        visible = true,
                        enter   = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec  = androidx.compose.animation.core.tween(
                                durationMillis = 300 + index * 30
                            )
                        )
                    ) {
                        StationCard(
                            station         = station.copy(
                                isFavorite = uiState.favorites.any { it.stationuuid == station.stationuuid }
                            ),
                            isPlaying       = uiState.selectedStation?.stationuuid == station.stationuuid
                                    && uiState.isPlaying,
                            onPlay          = { onPlayStation(station) },
                            onToggleFavorite = { onToggleFavorite(station) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) } // espacio para bottom nav
            }
        }
    }
}
