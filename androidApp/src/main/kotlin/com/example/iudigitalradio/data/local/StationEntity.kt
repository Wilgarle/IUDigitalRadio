package com.example.iudigitalradio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.iudigitalradio.domain.model.RadioStation

/**
 * Entity de Room para las emisoras favoritas guardadas localmente.
 * Cumple constraint: "Los favoritos los maneja Room — no memoria volátil."
 */
@Entity(tableName = "favorite_stations")
data class StationEntity(
    @PrimaryKey
    val stationuuid: String,
    val name: String,
    val url: String,
    val country: String,
    val countrycode: String,
    val favicon: String,
    val tags: String,
    val votes: Int = 0,
    val clickcount: Int = 0,
    val language: String = "",
    val bitrate: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null
)

/** Entity → Domain Model */
fun StationEntity.toDomain(): RadioStation = RadioStation(
    stationuuid = stationuuid,
    name        = name,
    url         = url,
    country     = country,
    countrycode = countrycode,
    favicon     = favicon,
    tags        = tags,
    votes       = votes,
    clickcount  = clickcount,
    language    = language,
    bitrate     = bitrate,
    latitude    = latitude,
    longitude   = longitude,
    isFavorite  = true
)

/** Domain Model → Entity */
fun RadioStation.toEntity(): StationEntity = StationEntity(
    stationuuid = stationuuid,
    name        = name,
    url         = url,
    country     = country,
    countrycode = countrycode,
    favicon     = favicon,
    tags        = tags,
    votes       = votes,
    clickcount  = clickcount,
    language    = language,
    bitrate     = bitrate,
    latitude    = latitude,
    longitude   = longitude
)
