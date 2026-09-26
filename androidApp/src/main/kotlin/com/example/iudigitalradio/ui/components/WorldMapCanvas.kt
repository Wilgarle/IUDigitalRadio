package com.example.iudigitalradio.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.ui.theme.BackgroundDeep
import com.example.iudigitalradio.ui.theme.CyanBright
import com.example.iudigitalradio.ui.theme.PlayingGreen
import com.example.iudigitalradio.ui.theme.SurfaceDark

@Composable
fun WorldMapCanvas(
    stations: List<RadioStation>,
    playingStation: RadioStation?,
    onStationTap: (RadioStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue  = 1.2f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse_scale"
    )

    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "ripple"
    )

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 10f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        // Envolvemos fondo y puntos en un mismo graphicsLayer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }
        ) {
            // Fondo mapamundi (Equirectangular)
            AsyncImage(
                model = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c3/Equirectangular_projection_SW.jpg/1024px-Equirectangular_projection_SW.jpg",
                contentDescription = "Mapamundi",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds, // Ajusta a la pantalla
                alpha = 0.3f
            )

            // Canvas para puntos de emisoras
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(stations) {
                        detectTapGestures { tapOffset ->
                            // En el graphicsLayer, el Canvas mantiene su layout size
                            // pero el toque viene ya escalado por el modifier graphicsLayer.
                            // Espera, detectTapGestures obtiene coordinadas relativas al Canvas en su tamaño original, 
                            // por lo que no hace falta dividir por `scale`! (El graphicsLayer transforma visualmente y ajusta el pointer dispatching).
                            var closestStation: RadioStation? = null
                            var minDist = 40f
                            stations.filter { it.hasCoordinates }.forEach { station ->
                                val (px, py) = latLonToPixel(
                                    station.latitude!!, station.longitude!!,
                                    size.width.toFloat(), size.height.toFloat()
                                )
                                val dist = distance(tapOffset.x, tapOffset.y, px, py)
                                if (dist < minDist) {
                                    minDist = dist
                                    closestStation = station
                                }
                            }
                            closestStation?.let { onStationTap(it) }
                        }
                    }
            ) {
                // Dibujamos las emisoras (las coordenadas son del tamaño de la pantalla original)
                stations.filter { it.hasCoordinates }.forEach { station ->
                    val (px, py) = latLonToPixel(
                        station.latitude!!, station.longitude!!,
                        size.width, size.height
                    )
                    val isPlaying = station.stationuuid == playingStation?.stationuuid

                    if (isPlaying) {
                        for (ring in 0..2) {
                            val ringProgress = (rippleProgress + ring * 0.33f) % 1f
                            val ringRadius   = 8f + ringProgress * 24f
                            val ringAlpha    = (1f - ringProgress).coerceIn(0f, 1f)
                            drawCircle(
                                color  = PlayingGreen.copy(alpha = ringAlpha * 0.5f),
                                radius = ringRadius,
                                center = Offset(px, py)
                            )
                        }
                        drawCircle(color = PlayingGreen, radius = 6f * pulseScale, center = Offset(px, py))
                    } else {
                        drawCircle(color = CyanBright.copy(alpha = 0.7f), radius = 3f * pulseScale, center = Offset(px, py))
                    }
                }
            }
        }
    }
}

/**
 * Proyección equirectangular simple: lat/lon → pixel (x, y).
 */
private fun latLonToPixel(lat: Double, lon: Double, width: Float, height: Float): Pair<Float, Float> {
    val x = ((lon + 180.0) / 360.0 * width).toFloat()
    val y = ((90.0  - lat)  / 180.0 * height).toFloat()
    return Pair(x, y)
}

private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return kotlin.math.sqrt(dx * dx + dy * dy)
}
