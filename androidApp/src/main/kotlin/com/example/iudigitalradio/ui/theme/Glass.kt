package com.example.iudigitalradio.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ─── Tokens del sistema de diseño "Aetheric Lumina" ──────────────────────────
object Glass {
    // Dark mode: blanco translúcido sobre oscuro
    val Level1  = Color(0x08FFFFFF)   // rgba(255,255,255,0.03) – cards estáticas
    val Level2  = Color(0x12FFFFFF)   // rgba(255,255,255,0.07) – floating elements
    val Level3  = Color(0x1FFFFFFF)   // rgba(255,255,255,0.12) – active / interactive
    val Border  = Color(0x14FFFFFF)   // borde sutil
    val BorderActive = Color(0x2600D4FF) // borde cyan traslúcido

    // Light mode: negro translúcido sobre claro
    val Level1Light  = Color(0x0A000000)
    val Level2Light  = Color(0x14000000)
    val Level3Light  = Color(0x20000000)
    val BorderLight  = Color(0x18000000)
    val BorderActiveLight = Color(0x30006978)
}

/** Aplica estilo glass card a cualquier Modifier */
fun Modifier.glassCard(
    level: Color = Glass.Level1,
    borderColor: Color = Glass.Border,
    radius: Int = 16
): Modifier = this
    .background(level, RoundedCornerShape(radius.dp))
    .border(0.7.dp, borderColor, RoundedCornerShape(radius.dp))

