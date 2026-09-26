package com.example.iudigitalradio.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.iudigitalradio.presentation.discover.DiscoverScreen
import com.example.iudigitalradio.presentation.favorites.FavoritesScreen
import com.example.iudigitalradio.presentation.map.WorldMapScreen
import com.example.iudigitalradio.presentation.player.PlayerViewModel
import com.example.iudigitalradio.presentation.profile.ProfileScreen
import com.example.iudigitalradio.presentation.search.SearchScreen
import kotlinx.serialization.Serializable

// ── Type-safe navigation routes (RF compatible, Navigation Compose 2.8.9+) ───

@Serializable object RouteDiscover
@Serializable object RouteWorldMap
@Serializable object RouteFavorites
@Serializable object RouteSearch
@Serializable object RouteProfile

/**
 * NavHost principal de la aplicación.
 * Routes type-safe con @Serializable — sin strings mágicas.
 * El PlayerViewModel se comparte entre pantallas (Activity scope).
 */
@Composable
fun RadioNavHost(
    navController: NavHostController,
    playerViewModel: PlayerViewModel
) {
    val uiState by playerViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = RouteDiscover
    ) {
        composable<RouteDiscover> {
            DiscoverScreen(
                uiState     = uiState,
                onPlayStation  = playerViewModel::playStation,
                onTogglePlay   = playerViewModel::togglePlayPause,
                onToggleMute   = playerViewModel::toggleMute,
                onToggleFavorite = playerViewModel::toggleFavorite,
                onNextStation  = playerViewModel::playNext,
                onPrevStation  = playerViewModel::playPrevious,
                onSearch       = playerViewModel::search,
                onSetVolume    = playerViewModel::setVolume,
                onSetUserPhoto = playerViewModel::setUserPhoto,
                onNavigateToMap = { navController.navigate(RouteWorldMap) },
                onNavigateToSearch = { navController.navigate(RouteSearch) },
                onNavigateToFavorites = { navController.navigate(RouteFavorites) }
            )
        }

        composable<RouteWorldMap> {
            WorldMapScreen(
                playingStation = uiState.selectedStation,
                onPlayStation  = playerViewModel::playStation,
                onBack         = navController::navigateUp
            )
        }

        composable<RouteFavorites> {
            FavoritesScreen(
                onPlayStation     = playerViewModel::playStation,
                onToggleFavorite  = playerViewModel::toggleFavorite,
                onBack            = navController::navigateUp
            )
        }

        composable<RouteSearch> {
            SearchScreen(
                onPlayStation  = playerViewModel::playStation,
                onToggleFavorite = playerViewModel::toggleFavorite,
                favorites      = uiState.favorites,
                onBack         = navController::navigateUp
            )
        }

        composable<RouteProfile> {
            ProfileScreen(
                userPhoto = uiState.userPhoto,
                onPhotoTaken = playerViewModel::setUserPhoto,
                listenedSeconds = uiState.listenedSeconds,
                listenedCountriesCount = uiState.listenedCountries.size,
                favoritesCount = uiState.favorites.size,
                isDarkMode = uiState.isDarkMode,
                isHapticEnabled = uiState.isVibrationEnabled,
                onDarkModeToggle = playerViewModel::setDarkMode,
                onHapticToggle = playerViewModel::setVibrationEnabled,
                onBack = navController::navigateUp
            )
        }
    }
}
