package com.example.iudigitalradio.presentation.profile

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.iudigitalradio.ui.components.UserAvatarButton
import com.example.iudigitalradio.ui.theme.CyanBright
import com.example.iudigitalradio.ui.theme.VioletElectric
import java.util.Locale

private val ErrorRed = Color(0xFFFF4B4B)

/**
 * Pantalla de Perfil Premium — Diseño "Aetheric Lumina" adaptativo (Dark & Light mode).
 * Muestra métricas reales de escucha (Favoritas, Países, Horas) sincronizadas en tiempo real.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userPhoto: Bitmap?,
    onPhotoTaken: (Any?) -> Unit,
    listenedSeconds: Long = 0L,
    listenedCountriesCount: Int = 0,
    favoritesCount: Int = 0,
    isDarkMode: Boolean = true,
    isHapticEnabled: Boolean = true,
    onDarkModeToggle: (Boolean) -> Unit = {},
    onHapticToggle: (Boolean) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit = {}
) {
    // Formateo dinámico del tiempo de escucha
    val hoursFormatted = when {
        listenedSeconds == 0L -> "0h"
        listenedSeconds < 60L -> "${listenedSeconds}s"
        listenedSeconds < 3600L -> "${listenedSeconds / 60}m"
        else -> String.format(Locale.US, "%.1fh", listenedSeconds / 3600.0)
    }

    // Animación de pulso en el anillo del avatar
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val cardBg = if (isDarkMode) Color(0x16FFFFFF) else MaterialTheme.colorScheme.surface
    val cardBorder = if (isDarkMode) Color(0x1FFFFFFF) else Color(0x18000000)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Halos de fondo atmosféricos sutiles ────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val alphaMult = if (isDarkMode) 1f else 0.5f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyanBright.copy(alpha = 0.12f * alphaMult), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.15f),
                    radius = size.width * 0.7f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(VioletElectric.copy(alpha = 0.08f * alphaMult), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.6f),
                    radius = size.width * 0.5f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── TopAppBar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Mi Perfil",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(Modifier.height(8.dp))

            // ── Avatar con anillo Cyan pulsante y tamaño ajustado ─────────────
            Box(
                modifier = Modifier.size(136.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow ring exterior animado
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CyanBright.copy(alpha = glowAlpha * 0.45f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Avatar central
                Box(
                    modifier = Modifier
                        .size(124.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, CyanBright.copy(alpha = glowAlpha), CircleShape)
                        .background(cardBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatarButton(
                        userPhoto = userPhoto,
                        onPhotoTaken = onPhotoTaken,
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.size(124.dp)
                    )
                }

                // Camera badge indicador
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Nombre y Email centrados ───────────────────────────────────────
            Text(
                text = "Usuario",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "usuario@iudigital.edu.co",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // ── Fila de Métricas Reales Centradas ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = favoritesCount.toString(),
                    label = "Favoritas",
                    modifier = Modifier.weight(1f),
                    background = cardBg,
                    borderColor = cardBorder
                )
                StatCard(
                    value = listenedCountriesCount.toString(),
                    label = "Países",
                    modifier = Modifier.weight(1f),
                    background = cardBg,
                    borderColor = cardBorder
                )
                StatCard(
                    value = hoursFormatted,
                    label = "Horas",
                    modifier = Modifier.weight(1f),
                    background = cardBg,
                    borderColor = cardBorder
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Ajustes Glass Card ─────────────────────────────────────────────
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                background = cardBg,
                borderColor = cardBorder
            ) {
                Column {
                    SettingRowNav(
                        icon = Icons.Filled.Notifications,
                        label = "Notificaciones"
                    )
                    GlassDivider(borderColor = cardBorder)
                    SettingRowToggle(
                        icon = Icons.Filled.DarkMode,
                        label = "Modo oscuro",
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeToggle
                    )
                    GlassDivider(borderColor = cardBorder)
                    SettingRowToggle(
                        icon = Icons.Filled.Vibration,
                        label = "Háptica",
                        checked = isHapticEnabled,
                        onCheckedChange = onHapticToggle
                    )
                    GlassDivider(borderColor = cardBorder)
                    SettingRowNav(
                        icon = Icons.Filled.Info,
                        label = "Acerca de",
                        subtitle = "v1.0.0 — IU Digital Radio"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Cerrar Sesión ──────────────────────────────────────────────────
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                borderColor = ErrorRed.copy(0.35f),
                background = ErrorRed.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Cerrar sesión",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = ErrorRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Componentes del Design System ───────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    GlassCard(
        modifier = modifier,
        background = background,
        borderColor = borderColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SettingRowNav(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SettingRowToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.background,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun GlassDivider(borderColor: Color = MaterialTheme.colorScheme.outlineVariant) {
    HorizontalDivider(color = borderColor, thickness = 0.5.dp)
}
