package com.example.iudigitalradio.presentation.map

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.iudigitalradio.domain.model.RadioStation

import com.example.iudigitalradio.ui.theme.BackgroundDeep
import com.example.iudigitalradio.ui.theme.CyanBright
import com.example.iudigitalradio.ui.theme.SurfaceDark

/**
 * Premium: Pantalla del mapa mundial animado de emisoras.
 * Usa WorldMapCanvas (Canvas Compose con proyección Mercator).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapScreen(
    playingStation: RadioStation?,
    onPlayStation: (RadioStation) -> Unit,
    onBack: () -> Unit,
    viewModel: WorldMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // ── Marcador personalizado: pin teardrop con 📻 icon ──────────────────────
    val radioMarkerBitmap = remember {
        val size = 72
        val bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        val cyanArgb = android.graphics.Color.parseColor("#00D4FF")
        val deepArgb = android.graphics.Color.parseColor("#0A0A0F")

        // Dibujar teardrop (pin invertido)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = cyanArgb
            style = android.graphics.Paint.Style.FILL
        }
        val cx = size / 2f
        val r = size * 0.38f
        val tipY = size.toFloat() - 4f
        val topY = r
        val path = android.graphics.Path().apply {
            addCircle(cx, topY, r, android.graphics.Path.Direction.CW)
            moveTo(cx - r * 0.45f, topY + r * 0.75f)
            lineTo(cx, tipY)
            lineTo(cx + r * 0.45f, topY + r * 0.75f)
            close()
        }
        canvas.drawPath(path, paint)

        // Círculo blanco interior
        val innerPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = deepArgb; style = android.graphics.Paint.Style.FILL
        }
        canvas.drawCircle(cx, topY, r * 0.60f, innerPaint)

        // Ícono de radio "📻" como texto centrado
        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = cyanArgb
            textSize = r * 0.85f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("📻", cx, topY + textPaint.textSize * 0.35f, textPaint)

        bmp
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // TopAppBar
        Column {
            TopAppBar(
                title = {
                    Text("Mapa Mundial de Emisoras", color = CyanBright,
                        style = MaterialTheme.typography.titleMedium)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = CyanBright)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // Proveedor de Mapas CARTO (Dark Matter)
            // Aquí podrás añadir la API key si CARTO lo requiere
            val cartoDarkSource = androidx.compose.runtime.remember {
                object : org.osmdroid.tileprovider.tilesource.XYTileSource(
                    "CartoDark", 1, 20, 256, ".png",
                    arrayOf(
                        "https://a.basemaps.cartocdn.com/rastertiles/dark_all/",
                        "https://b.basemaps.cartocdn.com/rastertiles/dark_all/",
                        "https://c.basemaps.cartocdn.com/rastertiles/dark_all/"
                    ),
                    "© OpenStreetMap contributors, © CARTO"
                ) {
                    override fun getTileURLString(pMapTileIndex: Long): String {
                        val z = org.osmdroid.util.MapTileIndex.getZoom(pMapTileIndex)
                        val x = org.osmdroid.util.MapTileIndex.getX(pMapTileIndex)
                        val y = org.osmdroid.util.MapTileIndex.getY(pMapTileIndex)
                        val apiKey = "eyJhbGciOiJIUzI1NiJ9.eyJhIjoiYWNfaDU1Nm41Y3AiLCJqdGkiOiI4NTUyNWZhZCJ9._tgdfZB2FrbHYip0Z6X7YL_7O8QdyQkFfqmhxWMKfsc"
                        return "https://a.basemaps.cartocdn.com/rastertiles/dark_all/$z/$x/$y.png?key=$apiKey"
                    }
                }
            }

            // Mapa Nativo OSMDroid
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        org.osmdroid.views.MapView(ctx).apply {
                            setTileSource(cartoDarkSource)
                            setMultiTouchControls(true)
                            controller.setZoom(5.5)
                            controller.setCenter(org.osmdroid.util.GeoPoint(4.5, -73.5))
                            
                            // Bloquear repetición y scroll infinito para que parezca global
                            isHorizontalMapRepetitionEnabled = false
                            isVerticalMapRepetitionEnabled = false
                            setScrollableAreaLimitDouble(org.osmdroid.util.BoundingBox(85.0, 180.0, -85.0, -180.0))
                            minZoomLevel = 2.8
                        }
                    },
                        update = { mapView ->
                            mapView.overlays.clear()
                            uiState.stations.forEach { station ->
                                if (station.latitude != null && station.longitude != null) {
                                    val marker = org.osmdroid.views.overlay.Marker(mapView).apply {
                                        position = org.osmdroid.util.GeoPoint(station.latitude, station.longitude)
                                        title = station.name
                                        snippet = station.country
                                        icon = android.graphics.drawable.BitmapDrawable(
                                            context.resources, radioMarkerBitmap
                                        )
                                        setAnchor(
                                            org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                                            org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
                                        )
                                        setOnMarkerClickListener { m, _ ->
                                            m.showInfoWindow()
                                            viewModel.selectStation(station)
                                            true
                                        }
                                    }
                                    mapView.overlays.add(marker)
                                }
                            }
                            mapView.invalidate()
                        },
                    modifier = Modifier.fillMaxSize()
                )

                if (uiState.isLoading) {
                    CircularProgressIndicator(color = CyanBright, modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        // Popup de emisora seleccionada
        uiState.selectedStation?.let { station ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape  = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = station.name, style = MaterialTheme.typography.titleSmall, color = CyanBright)
                        Text(text = "${station.flagEmoji} ${station.country}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (station.bitrateLabel.isNotBlank()) {
                            Text(text = station.bitrateLabel, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = {
                        onPlayStation(station)
                        viewModel.selectStation(null)
                    }) {
                        Box(modifier = Modifier.background(CyanBright, shape = androidx.compose.foundation.shape.CircleShape)) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Reproducir ${station.name}",
                                tint = BackgroundDeep, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }

        // Badge contador
        if (!uiState.isLoading && uiState.stations.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 16.dp)
                    .background(CyanBright.copy(0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text  = "${uiState.stations.size} emisoras",
                    color = CyanBright,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
