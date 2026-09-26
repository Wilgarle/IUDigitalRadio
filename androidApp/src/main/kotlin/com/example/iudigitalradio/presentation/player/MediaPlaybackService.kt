package com.example.iudigitalradio.presentation.player

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * RF-07: MediaSessionService para streaming de audio en background.
 * Proporciona la notificación del sistema y controles de medios.
 *
 * CONSTRAINT CRÍTICO: android:exported="true" declarado en AndroidManifest.xml
 * CONSTRAINT CRÍTICO: ExoPlayer liberado en onDestroy() — el Service es el propietario.
 *
 * No hereda de ninguna clase Android legacy — usa Media3 puro.
 */
@AndroidEntryPoint
class MediaPlaybackService : MediaSessionService() {

    /** ExoPlayer singleton — inyectado por Hilt desde AppModule */
    @Inject lateinit var player: ExoPlayer

    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, player)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        // CONSTRAINT CRÍTICO: ExoPlayer liberado en onDestroy()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
