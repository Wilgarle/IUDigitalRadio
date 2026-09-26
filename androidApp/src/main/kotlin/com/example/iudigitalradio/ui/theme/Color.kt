package com.example.iudigitalradio.ui.theme

import androidx.compose.ui.graphics.Color

// ── Fondos ────────────────────────────────────────────────────────────────────
val BackgroundDeep = Color(0xFF0D0D1A)      // Fondo principal oscuro profundo
val BackgroundDark = Color(0xFF1A1A2E)      // Fondo secundario azul eléctrico oscuro
val SurfaceDark    = Color(0xFF16213E)      // Superficie de Cards
val SurfaceVariant = Color(0xFF0F3460)      // Superficie de elementos elevados

// ── Acentos principales ───────────────────────────────────────────────────────
val CyanBright   = Color(0xFF00E5FF)        // Acento principal: cian brillante (elemento activo)
val CyanMedium   = Color(0xFF00B0FF)        // Cian intermedio
val CyanDim      = Color(0xFF0097A7)        // Cian atenuado

// ── Acentos secundarios ───────────────────────────────────────────────────────
val VioletElectric = Color(0xFF7C4DFF)      // Acento secundario: violeta eléctrico (favoritos)
val VioletMedium   = Color(0xFF651FFF)      // Violeta intermedio
val VioletDim      = Color(0xFF4527A0)      // Violeta atenuado

// ── Texto ─────────────────────────────────────────────────────────────────────
val TextPrimary   = Color(0xFFE0E0E0)       // Texto principal
val TextSecondary = Color(0xFF9E9E9E)       // Texto secundario / placeholders
val TextDisabled  = Color(0xFF616161)       // Texto deshabilitado

// ── Estado ────────────────────────────────────────────────────────────────────
val PlayingGreen = Color(0xFF00E676)        // Emisora en reproducción
val FavoriteRed  = Color(0xFFFF1744)        // Corazón favorito
val BitrateBadge = Color(0xFF1E2A38)        // Badge de bitrate

// ── Gradiente ─────────────────────────────────────────────────────────────────
val GradientStart = Color(0xFF0D0D1A)
val GradientEnd   = Color(0xFF1A1A2E)

// ── Material Color Scheme tokens ─────────────────────────────────────────────
val Primary       = CyanBright
val OnPrimary     = Color(0xFF003344)
val Secondary     = VioletElectric
val OnSecondary   = Color(0xFF1A003C)
val Tertiary      = PlayingGreen
val OnTertiary    = Color(0xFF003318)
val Background    = BackgroundDeep
val OnBackground  = TextPrimary
val Surface       = SurfaceDark
val OnSurface     = TextPrimary
val Error         = FavoriteRed
val OnError       = Color(0xFF370008)
