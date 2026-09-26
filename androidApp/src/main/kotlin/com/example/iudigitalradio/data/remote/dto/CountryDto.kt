package com.example.iudigitalradio.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CountryDto(
    @SerializedName("name") val name: String,
    @SerializedName("iso_3166_1") val isoCode: String?,
    @SerializedName("stationcount") val stationCount: Int?
)
