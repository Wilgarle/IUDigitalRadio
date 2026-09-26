package com.example.iudigitalradio.ui.components

import android.media.audiofx.Visualizer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.iudigitalradio.ui.theme.CyanBright
import com.example.iudigitalradio.ui.theme.VioletElectric
import kotlin.math.abs
import kotlin.math.sin

/**
 * Visualizador de audio premium — 12 barras con efecto Neon Bloom de 3 capas.
 * Soporta datos reales del Visualizer API de Android (audioSessionId != 0),
 * o animación sintética de fallback con ondas de fase diferente por barra.
 */
@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = CyanBright,
    barCount: Int = 12,
    audioSessionId: Int = 0
) {
    // ── Datos del Visualizer API ──────────────────────────────────────────────
    var waveformData by remember { mutableStateOf(FloatArray(barCount) { 0f }) }

    DisposableEffect(audioSessionId, isPlaying) {
        if (audioSessionId != 0 && isPlaying) {
            val visualizer = try {
                Visualizer(audioSessionId).apply {
                    captureSize = Visualizer.getCaptureSizeRange()[1]
                    setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(v: Visualizer, waveform: ByteArray, samplingRate: Int) {
                            val chunkSize = waveform.size / barCount
                            val newData = FloatArray(barCount) { i ->
                                val chunk = waveform.slice(i * chunkSize until (i + 1) * chunkSize)
                                val avg = chunk.map { abs(it.toInt()) }.average().toFloat()
                                (avg / 128f).coerceIn(0.05f, 1f)
                            }
                            waveformData = newData
                        }
                        override fun onFftDataCapture(v: Visualizer, fft: ByteArray, samplingRate: Int) {}
                    }, Visualizer.getMaxCaptureRate() / 2, true, false)
                    enabled = true
                }
            } catch (e: Exception) { null }

            onDispose {
                visualizer?.enabled = false
                visualizer?.release()
                waveformData = FloatArray(barCount) { 0f }
            }
        } else {
            onDispose {}
        }
    }

    // ── Animación sintética de fallback ───────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    val phases = List(barCount) { i ->
        val anim by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 450 + i * 80, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "bar_phase_$i"
        )
        anim
    }

    Canvas(modifier = modifier) {
        val W = size.width
        val H = size.height
        val gap = W * 0.025f
        val barWidth = (W - gap * (barCount - 1)) / barCount
        val maxH = H * 0.95f
        val minH = H * 0.10f

        for (i in 0 until barCount) {
            // Si tenemos datos reales los usamos; sino animación sintética
            val heightFraction = if (isPlaying) {
                val realVal = waveformData.getOrElse(i) { 0f }
                if (realVal > 0.08f) {
                    realVal  // datos reales del audio
                } else {
                    // fallback: ondas de seno con 3 armónicos (suena más orgánico)
                    val p = phases[i]
                    val s = (sin(p) * 0.5f + sin(p * 2.1f) * 0.3f + sin(p * 3.7f) * 0.2f + 1f) / 2f
                    0.15f + s * 0.85f
                }
            } else {
                0.08f
            }

            val barH = minH + (maxH - minH) * heightFraction.coerceIn(0f, 1f)
            val t = i.toFloat() / (barCount - 1)
            val r = barColor.red * (1 - t) + VioletElectric.red * t
            val g = barColor.green * (1 - t) + VioletElectric.green * t
            val b = barColor.blue * (1 - t) + VioletElectric.blue * t
            val color = Color(r, g, b)

            val left = i * (barWidth + gap)
            val top = H - barH
            val cr = CornerRadius(barWidth / 2, barWidth / 2)

            // Capa 1: Glow difuso exterior (bloom)
            drawRoundRect(
                color = color.copy(alpha = 0.22f),
                topLeft = Offset(left - barWidth * 0.35f, top - barH * 0.04f),
                size = Size(barWidth * 1.7f, barH * 1.04f),
                cornerRadius = cr
            )
            // Capa 2: Barra sólida principal
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(barWidth, barH),
                cornerRadius = cr
            )
            // Capa 3: Specular highlight blanco en la punta
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(left + barWidth * 0.15f, top),
                size = Size(barWidth * 0.35f, barH * 0.12f),
                cornerRadius = CornerRadius(barWidth / 4, barWidth / 4)
            )
        }
    }
}
