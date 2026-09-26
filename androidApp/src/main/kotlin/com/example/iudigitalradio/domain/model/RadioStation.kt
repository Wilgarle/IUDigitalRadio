package com.example.iudigitalradio.domain.model

/**
 * Modelo de dominio puro — sin dependencias de Android ni de frameworks.
 * Mapea el modelo de la Radio Browser API con campos adicionales de favoritos.
 */
data class RadioStation(
    val stationuuid: String,
    val name: String,
    val url: String,            // URL del stream (url_resolved o url)
    val country: String,
    val countrycode: String,    // Código ISO-2 para el mapa
    val favicon: String,        // Logo/icono de la emisora
    val tags: String,           // Géneros musicales (separados por coma)
    val votes: Int,
    val clickcount: Int,
    val language: String,
    val bitrate: Int,
    val latitude: Double?,      // Para el mapa mundial
    val longitude: Double?,     // Para el mapa mundial
    val isFavorite: Boolean = false
) {
    /** Lista de tags como items individuales (para Chips en la UI) */
    val tagList: List<String>
        get() = tags.split(",", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(5)

    /** Bandera emoji a partir del código ISO-2 del país */
    val flagEmoji: String
        get() {
            val code = countrycode.uppercase()
            if (code.length != 2) return ""
            val first  = 0x1F1E6 + (code[0].code - 'A'.code)
            val second = 0x1F1E6 + (code[1].code - 'A'.code)
            return String(Character.toChars(first)) + String(Character.toChars(second))
        }

    /** True si tiene coordenadas válidas para el mapa */
    val hasCoordinates: Boolean
        get() = latitude != null && longitude != null
                && latitude in -90.0..90.0
                && longitude in -180.0..180.0

    /** Descripción de bitrate para la UI */
    val bitrateLabel: String
        get() = if (bitrate > 0) "${bitrate}kbps" else ""
}
