package com.example.iudigitalradio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import coil3.compose.AsyncImage
import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.ui.theme.BitrateBadge
import com.example.iudigitalradio.ui.theme.CyanBright
import com.example.iudigitalradio.ui.theme.FavoriteRed
import com.example.iudigitalradio.ui.theme.Glass
import com.example.iudigitalradio.ui.theme.PlayingGreen
import com.example.iudigitalradio.ui.theme.SurfaceVariant

/**
 * RF-06: Item de la LazyColumn de emisoras.
 * Muestra: favicon (Coil3), nombre, país+bandera, tags (Chips scrollables), bitrate badge, Play.
 * Animación de entrada: slideInVertically (aplicada desde LazyColumn).
 * rememberSaveable preserva estado de expansión ante rotaciones (RF-04 compatible).
 */
@Composable
fun StationCard(
    station: RadioStation,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    // RF-04: rememberSaveable preserva el estado de expansión de tags en rotaciones
    var tagsExpanded by rememberSaveable(station.stationuuid) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isPlaying) Glass.Level3 else Glass.Level1)
            .border(
                width = if (isPlaying) 1.dp else 0.5.dp,
                color = if (isPlaying) CyanBright.copy(alpha = 0.4f) else Glass.Border,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onPlay() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Favicon de la emisora (Coil3) ────────────────────────────────
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (station.favicon.isNotBlank()) {
                    AsyncImage(
                        model            = station.favicon,
                        contentDescription = "Logo ${station.name}",
                        contentScale     = ContentScale.Crop,
                        modifier         = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    // Fallback: inicial del nombre
                    Text(
                        text  = station.name.firstOrNull()?.uppercase() ?: "R",
                        color = CyanBright,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── Información de la emisora ────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text     = station.name,
                        style    = MaterialTheme.typography.titleSmall,
                        color    = if (isPlaying) CyanBright else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isPlaying) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(PlayingGreen, CircleShape)
                        )
                    }
                }

                // País + bandera
                Text(
                    text  = "${station.flagEmoji} ${station.country}".trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tags como chips scrollables
                if (station.tagList.isNotEmpty()) {
                    Row(
                        modifier  = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        station.tagList.take(4).forEach { tag ->
                            FilterChip(
                                selected = false,
                                onClick  = {},
                                label    = { Text(tag, fontSize = 10.sp) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    containerColor = BitrateBadge,
                                    labelColor     = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ── Controles ────────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Bitrate badge
                if (station.bitrateLabel.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .background(BitrateBadge, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text  = station.bitrateLabel,
                            color = CyanBright,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Favorito
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (station.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorita",
                        tint   = if (station.isFavorite) FavoriteRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Play
                IconButton(onClick = onPlay, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Reproducir ${station.name}",
                        tint     = if (isPlaying) PlayingGreen else CyanBright,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
