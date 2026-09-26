package com.example.iudigitalradio.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Color Scheme oscuro eléctrico personalizado.
 * Paleta: Gradiente oscuro azul eléctrico → negro
 * Acento principal: Cian brillante (#00E5FF).
 * Acento secundario: Violeta eléctrico (#7C4DFF).
 */
private val ElectricDarkColorScheme = darkColorScheme(
    primary            = CyanBright,
    onPrimary          = OnPrimary,
    primaryContainer   = CyanDim,
    onPrimaryContainer = TextPrimary,
    secondary          = VioletElectric,
    onSecondary        = OnSecondary,
    secondaryContainer = VioletDim,
    onSecondaryContainer = TextPrimary,
    tertiary           = PlayingGreen,
    onTertiary         = OnTertiary,
    background         = BackgroundDeep,
    onBackground       = TextPrimary,
    surface            = SurfaceDark,
    onSurface          = TextPrimary,
    surfaceVariant     = SurfaceVariant,
    onSurfaceVariant   = TextSecondary,
    error              = FavoriteRed,
    onError            = OnError,
    outline            = CyanDim.copy(alpha = 0.5f),
    outlineVariant     = VioletDim.copy(alpha = 0.3f),
)

/**
 * Color Scheme claro premium — fondos cálidos ivory, acentos saturados,
 * alto contraste WCAG AA+, regla 60/30/10 aplicada.
 */
private val ElectricLightColorScheme = lightColorScheme(
    primary            = Color(0xFF006978), // Teal profundo — alto contraste sobre blanco
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFB2EBF2), // Cyan pastel suave
    onPrimaryContainer = Color(0xFF001F24),
    secondary          = Color(0xFF5C2D91), // Violeta oscuro saturado
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFE8DEF8), // Lavanda pastel
    onSecondaryContainer = Color(0xFF1D004E),
    tertiary           = Color(0xFF2E7D32), // Verde bosque
    onTertiary         = Color.White,
    background         = Color(0xFFF8F9FC), // Ivory azulado muy sutil
    onBackground       = Color(0xFF1A1C20), // Casi negro — máximo contraste
    surface            = Color(0xFFFFFFFF), // Cards blancas puras
    onSurface          = Color(0xFF1A1C20),
    surfaceVariant     = Color(0xFFECEFF5), // Gris azulado suave para cards secundarias
    onSurfaceVariant   = Color(0xFF44474E), // Gris medio — legible como subtítulo
    error              = Color(0xFFBA1A1A),
    onError            = Color.White,
    outline            = Color(0xFF74777F), // Bordes visibles en claro
    outlineVariant     = Color(0xFFC4C7D0), // Bordes suaves
)

/**
 * IU Digital Radio Theme con soporte completo de Dark/Light mode.
 * - darkMode: controla explícitamente el esquema de colores.
 * - En Android 12+ usa colores dinámicos del sistema si se solicita.
 */
@Composable
fun IUDigitalRadioTheme(
    darkMode: Boolean = true,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkMode) dynamicDarkColorScheme(LocalContext.current)
            else dynamicLightColorScheme(LocalContext.current)
        }
        darkMode -> ElectricDarkColorScheme
        else     -> ElectricLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = IURadioTypography,
        content     = content
    )
}
