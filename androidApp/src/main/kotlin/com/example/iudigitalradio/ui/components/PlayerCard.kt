package com.example.iudigitalradio.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import coil3.compose.AsyncImage
import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.ui.theme.BackgroundDeep
import com.example.iudigitalradio.ui.theme.CyanBright
import com.example.iudigitalradio.ui.theme.Glass
import com.example.iudigitalradio.ui.theme.PlayingGreen
import com.example.iudigitalradio.ui.theme.SurfaceDark
import com.example.iudigitalradio.ui.theme.VioletElectric
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * RF-07: Tarjeta del Reproductor Principal.
 * RF-05: Vibración háptica de 50ms en Play, Pause y Mute.
 * Premium: AudioVisualizer con barras Canvas animadas.
 */
@Composable
fun PlayerCard(
    station: RadioStation?,
    isPlaying: Boolean,
    isMuted: Boolean,
    isFavorite: Boolean = false,
    volume: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onMute: () -> Unit,
    onToggleFavorite: (() -> Unit)? = null,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Animación de elevación cuando está reproduciendo
    val elevation by animateDpAsState(
        targetValue = if (isPlaying) 16.dp else 4.dp,
        animationSpec = tween(300),
        label = "card_elevation"
    )

    val playPauseColor by animateColorAsState(
        targetValue = if (isPlaying) PlayingGreen else CyanBright,
        animationSpec = tween(200),
        label = "play_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Glass.Level2, Glass.Level1)
                )
            )
            .border(0.7.dp, Glass.Border, RoundedCornerShape(24.dp))
    ) {
        // Halo de glow bajo el contenido
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isPlaying) CyanBright.copy(alpha = 0.12f) else VioletElectric.copy(alpha = 0.06f),
                        androidx.compose.ui.graphics.Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.3f),
                    radius = size.width * 0.6f
                )
            )
        }
        Column(
            modifier            = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Favicon de la emisora seleccionada ────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(CyanBright.copy(0.2f), BackgroundDeep)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (station?.favicon?.isNotBlank() == true) {
                    AsyncImage(
                        model            = station.favicon,
                        contentDescription = "Logo ${station.name}",
                        contentScale     = ContentScale.Crop,
                        modifier         = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp))
                    )
                } else {
                    Text(
                        text       = if (station != null) station.name.take(2).uppercase() else "IU",
                        color      = CyanBright,
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Nombre, país y botón favorito ────────────────────────────────
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = station?.name ?: "Selecciona una emisora",
                    style      = MaterialTheme.typography.titleMedium,
                    color      = if (station != null) CyanBright else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    textAlign  = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f, fill = false)
                )
                if (station != null && onToggleFavorite != null) {
                    IconButton(
                        onClick = { vibrateHaptic(context, 30L); onToggleFavorite() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                            tint = if (isFavorite) Color(0xFFFF4D6D) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            if (station != null) {
                Text(
                    text  = "${station.flagEmoji} ${station.country}  •  ${station.bitrateLabel}".trim(' ', '•', ' '),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── RF-Premium: AudioVisualizer (20 barras Canvas animadas) ─────
            AudioVisualizer(
                isPlaying = isPlaying,
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 4.dp),
                barColor  = CyanBright,
                barCount  = 20
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Controles de reproducción ─────────────────────────────────────
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                // Anterior
                IconButton(onClick = {
                    vibrate(context)   // RF-05 no aplica aquí (solo Play/Pause/Mute)
                    onPrevious()
                }) {
                    Icon(
                        imageVector        = Icons.Filled.SkipPrevious,
                        contentDescription = "Anterior",
                        tint               = MaterialTheme.colorScheme.onSurface,
                        modifier           = Modifier.size(32.dp)
                    )
                }

                // RF-05: Play/Pause con vibración háptica de 50ms
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(playPauseColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        vibrateHaptic(context)  // RF-05: 50ms vibración
                        onPlayPause()
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint     = BackgroundDeep,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Siguiente
                IconButton(onClick = {
                    vibrate(context)
                    onNext()
                }) {
                    Icon(
                        imageVector        = Icons.Filled.SkipNext,
                        contentDescription = "Siguiente",
                        tint               = MaterialTheme.colorScheme.onSurface,
                        modifier           = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── RF-05: Mute con vibración + Slider de volumen ─────────────────
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    vibrateHaptic(context)  // RF-05: 50ms vibración en Mute
                    onMute()
                }) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMuted) "Activar audio" else "Silenciar",
                        tint = if (isMuted) VioletElectric else CyanBright,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Slider(
                    value         = if (isMuted) 0f else volume,
                    onValueChange = onVolumeChange,
                    valueRange    = 0f..1f,
                    modifier      = Modifier.weight(1f),
                    colors        = SliderDefaults.colors(
                        thumbColor        = CyanBright,
                        activeTrackColor  = CyanBright,
                        inactiveTrackColor = SurfaceDark
                    )
                )
            }
        }
    }
}

/**
 * RF-05: Vibración háptica de 50ms.
 * VibratorManager (API 31+) con fallback a Vibrator para API 30.
 */
fun vibrateHaptic(context: Context, durationMs: Long = 50L) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator
        vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }
}

/** Vibración genérica más suave para navegación */
private fun vibrate(context: Context) = vibrateHaptic(context, 30L)
