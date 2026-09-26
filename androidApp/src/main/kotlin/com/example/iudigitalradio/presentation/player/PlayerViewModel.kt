package com.example.iudigitalradio.presentation.player

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.net.Uri
import android.provider.MediaStore
import android.graphics.ImageDecoder
import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.domain.repository.RadioRepository
import com.example.iudigitalradio.domain.repository.UserPreferencesRepository
import com.example.iudigitalradio.domain.usecase.GetFavoriteStationsUseCase
import com.example.iudigitalradio.domain.usecase.GetTopStationsUseCase
import com.example.iudigitalradio.domain.usecase.SearchStationsUseCase
import com.example.iudigitalradio.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado UI principal de la aplicación (RF-04).
 * Incluye todos los campos requeridos: isPlaying, isMuted, selectedStation, userPhoto.
 */
data class RadioUiState(
    val isPlaying: Boolean = false,
    val isMuted: Boolean = false,
    val selectedStation: RadioStation? = null,
    val userPhoto: Bitmap? = null,
    val stations: List<RadioStation> = emptyList(),
    val favorites: List<RadioStation> = emptyList(),
    val mapStations: List<RadioStation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val volume: Float = 1f,
    val searchQuery: String = "",
    val isVibrationEnabled: Boolean = true,
    // ── Stats de escucha (persistidas en DataStore) ──────────────────────────
    val listenedSeconds: Long = 0L,
    val listenedCountries: Set<String> = emptySet(),
    val isDarkMode: Boolean = true
)

/**
 * ViewModel central de reproducción.
 * Gestiona StateFlow<RadioUiState> + controles de ExoPlayer (RF-04, RF-07).
 * ExoPlayer singleton inyectado por Hilt — el Service es el dueño del lifecycle de release.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer,
    private val getTopStationsUseCase: GetTopStationsUseCase,
    private val searchStationsUseCase: SearchStationsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getFavoriteStationsUseCase: GetFavoriteStationsUseCase,
    private val repository: RadioRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RadioUiState())
    /** RF-04: StateFlow<RadioUiState> */
    val uiState: StateFlow<RadioUiState> = _uiState.asStateFlow()

    private var mediaController: MediaController? = null
    // Tracking de tiempo de escucha
    private var playbackStartTime: Long = 0L
    private var listeningJob: kotlinx.coroutines.Job? = null

    init {
        connectToMediaService()
        loadTopStations()
        observeFavorites()
        loadMapStations()
        setupExoPlayerListener()
        observePreferences()
    }

    // ── Media3 Service Connection ──────────────────────────────────────────────

    private fun connectToMediaService() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MediaPlaybackService::class.java)
        )
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener(
            {
                runCatching {
                    mediaController = future.get()
                }
            },
            { runnable -> Handler(Looper.getMainLooper()).post(runnable) }
        )
    }

    private fun setupExoPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    playbackStartTime = System.currentTimeMillis()
                    startListeningTimer()
                } else {
                    listeningJob?.cancel()
                    accumulateListeningTime()
                    playbackStartTime = 0L
                }
            }
        })
    }

    private fun startListeningTimer() {
        listeningJob?.cancel()
        listeningJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                accumulateListeningTime()
            }
        }
    }

    private fun accumulateListeningTime() {
        if (playbackStartTime > 0L) {
            val now = System.currentTimeMillis()
            val elapsedSec = (now - playbackStartTime) / 1000L
            if (elapsedSec > 0) {
                playbackStartTime = now
                viewModelScope.launch {
                    userPreferencesRepository.addListenedSeconds(elapsedSec)
                }
            }
        }
    }

    // ── Carga de datos ─────────────────────────────────────────────────────────

    fun loadTopStations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getTopStationsUseCase()
                .onSuccess { stations ->
                    _uiState.update { it.copy(stations = stations, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun loadMapStations() {
        viewModelScope.launch {
            repository.getStationsForMap()
                .onSuccess { stations ->
                    _uiState.update { it.copy(mapStations = stations) }
                }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavoriteStationsUseCase().collect { favs ->
                _uiState.update { it.copy(favorites = favs) }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferencesRepository.isVibrationEnabled.collect { enabled ->
                _uiState.update { it.copy(isVibrationEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.isDarkMode.collect { enabled ->
                _uiState.update { it.copy(isDarkMode = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.listenedSeconds.collect { sec ->
                _uiState.update { it.copy(listenedSeconds = sec) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.listenedCountries.collect { countries ->
                _uiState.update { it.copy(listenedCountries = countries) }
            }
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setVibrationEnabled(enabled)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
        viewModelScope.launch {
            userPreferencesRepository.setDarkMode(enabled)
        }
    }

    // ── Controles de reproducción (RF-07) ──────────────────────────────────────

    fun playStation(station: RadioStation) {
        val mediaItem = MediaItem.fromUri(station.url)
        // Usa mediaController si disponible (via Service), sino ExoPlayer directo
        val player: Player = mediaController ?: exoPlayer
        
        // Vibrate only on new station if enabled
        if (_uiState.value.selectedStation?.stationuuid != station.stationuuid && _uiState.value.isVibrationEnabled) {
            triggerVibration()
        }

        playbackStartTime = System.currentTimeMillis()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        // Registra el país escuchado y persiste
        val country = station.country.trim().ifBlank { null }
        if (country != null) {
            viewModelScope.launch {
                userPreferencesRepository.addListenedCountry(country)
            }
        }
        _uiState.update { state ->
            state.copy(
                selectedStation = station,
                isPlaying = true,
                listenedCountries = if (country != null) state.listenedCountries + country else state.listenedCountries
            )
        }
        // Registra click en la API (fire-and-forget)
        viewModelScope.launch { repository.registerClick(station.stationuuid) }
    }

    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (e: Exception) {
            // Permiso no concedido o hardware no soportado
        }
    }

    /** RF-04: togglePlayPause actualiza isPlaying en StateFlow */
    fun togglePlayPause() {
        val player: Player = mediaController ?: exoPlayer
        if (player.isPlaying) {
            player.pause()
            _uiState.update { it.copy(isPlaying = false) }
        } else {
            player.play()
            _uiState.update { it.copy(isPlaying = true) }
        }
    }

    /** RF-04: toggleMute actualiza isMuted en StateFlow */
    fun toggleMute() {
        val newMuted = !_uiState.value.isMuted
        val player: Player = mediaController ?: exoPlayer
        player.volume = if (newMuted) 0f else _uiState.value.volume
        _uiState.update { it.copy(isMuted = newMuted) }
    }

    fun setVolume(volume: Float) {
        val player: Player = mediaController ?: exoPlayer
        player.volume = volume
        _uiState.update { it.copy(volume = volume, isMuted = volume == 0f) }
    }

    fun playNext() {
        val stations = _uiState.value.stations
        val current = _uiState.value.selectedStation ?: return
        val idx = stations.indexOfFirst { it.stationuuid == current.stationuuid }
        val next = stations.getOrNull((idx + 1) % stations.size) ?: return
        playStation(next)
    }

    fun playPrevious() {
        val stations = _uiState.value.stations
        val current = _uiState.value.selectedStation ?: return
        val idx = stations.indexOfFirst { it.stationuuid == current.stationuuid }
        val prev = stations.getOrNull(if (idx > 0) idx - 1 else stations.size - 1) ?: return
        playStation(prev)
    }

    // ── RF-02: Foto de usuario ─────────────────────────────────────────────────

    /** Actualiza el avatar de usuario (cámara, galería o null para eliminar) */
    fun setUserPhoto(photoData: Any?) {
        viewModelScope.launch {
            when (photoData) {
                is Bitmap -> _uiState.update { it.copy(userPhoto = photoData) }
                is Uri -> {
                    try {
                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(context.contentResolver, photoData)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(context.contentResolver, photoData)
                        }
                        _uiState.update { it.copy(userPhoto = bitmap) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                null -> _uiState.update { it.copy(userPhoto = null) }
            }
        }
    }

    // ── Favoritas ─────────────────────────────────────────────────────────────

    fun toggleFavorite(station: RadioStation) {
        viewModelScope.launch { toggleFavoriteUseCase(station) }
    }

    // ── Búsqueda ──────────────────────────────────────────────────────────────

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            loadTopStations()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            searchStationsUseCase(query)
                .onSuccess { stations ->
                    _uiState.update { it.copy(stations = stations, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    override fun onCleared() {
        mediaController?.release()
        // ExoPlayer NO se libera aquí — el MediaPlaybackService es el propietario
        super.onCleared()
    }
}
