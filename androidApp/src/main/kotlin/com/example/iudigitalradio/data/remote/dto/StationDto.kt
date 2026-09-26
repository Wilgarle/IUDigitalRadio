package com.example.iudigitalradio.data.remote.dto

import com.example.iudigitalradio.domain.model.RadioStation
import com.google.gson.annotations.SerializedName

/**
 * DTO que mapea el JSON de la Radio Browser API.
 * Documentación: https://api.radio-browser.info
 */
data class StationDto(
    @SerializedName("stationuuid") val stationuuid: String = "",
    @SerializedName("name")        val name: String = "",
    @SerializedName("url_resolved") val urlResolved: String = "",
    @SerializedName("url")         val url: String = "",
    @SerializedName("country")     val country: String = "",
    @SerializedName("countrycode") val countrycode: String = "",
    @SerializedName("favicon")     val favicon: String = "",
    @SerializedName("tags")        val tags: String = "",
    @SerializedName("votes")       val votes: Int = 0,
    @SerializedName("clickcount")  val clickcount: Int = 0,
    @SerializedName("language")    val language: String = "",
    @SerializedName("bitrate")     val bitrate: Int = 0,
    @SerializedName("geo_lat")     val latitude: Double? = null,
    @SerializedName("geo_long")    val longitude: Double? = null,
    @SerializedName("lastcheckok") val lastCheckOk: Int = 0
) {
    /**
     * Convierte el DTO al modelo de dominio.
     * Usa url_resolved como URL primaria (más confiable) con fallback a url.
     */
    fun toDomain(isFavorite: Boolean = false): RadioStation = RadioStation(
        stationuuid = stationuuid,
        name        = name.trim(),
        url         = urlResolved.ifBlank { url },
        country     = country,
        countrycode = countrycode.uppercase(),
        favicon     = favicon,
        tags        = tags,
        votes       = votes,
        clickcount  = clickcount,
        language    = language,
        bitrate     = bitrate,
        latitude    = latitude,
        longitude   = longitude,
        isFavorite  = isFavorite
    )
}
