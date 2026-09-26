package com.example.iudigitalradio.data.remote

import com.example.iudigitalradio.data.remote.dto.StationDto
import com.example.iudigitalradio.data.remote.dto.CountryDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface para la Radio Browser API.
 * Base URL: https://de1.api.radio-browser.info/
 * Documentación: https://api.radio-browser.info
 *
 * No requiere API Key. Más de 30,000 emisoras gratuitas.
 */
interface RadioBrowserApiService {

    /** Top 50 emisoras más escuchadas globalmente */
    @GET("json/stations/topclick/{limit}")
    suspend fun getTopStations(
        @Path("limit") limit: Int = 50
    ): List<StationDto>

    /**
     * Búsqueda de emisoras por nombre (RF-06 + Premium SearchBar).
     * hidebroken=true filtra emisoras con streams rotos.
     */
    @GET("json/stations/search")
    suspend fun searchStations(
        @Query("name")       name: String,
        @Query("limit")      limit: Int = 50,
        @Query("hidebroken") hidebroken: Boolean = true,
        @Query("order")      order: String = "votes"
    ): List<StationDto>

    /** Emisoras por país (código ISO-2: "CO", "US", "DE", etc.) */
    @GET("json/stations/bycountry/{country}")
    suspend fun getStationsByCountry(
        @Path("country") country: String
    ): List<StationDto>

    /** Emisoras por género/tag (ej: "pop", "rock", "jazz", "news") */
    @GET("json/stations/bytag/{tag}")
    suspend fun getStationsByTag(
        @Path("tag") tag: String
    ): List<StationDto>

    /**
     * Click vote — incrementa la popularidad de una emisora.
     * Llamar cuando el usuario selecciona/reproduce una emisora.
     */
    @POST("json/url/{stationuuid}")
    suspend fun voteStation(
        @Path("stationuuid") uuid: String
    )

    /** Obtiene emisoras con coordenadas válidas para el mapa mundial */
    @GET("json/stations/search")
    suspend fun getStationsWithCoordinates(
        @Query("countrycode")  countryCode: String? = null,
        @Query("has_geo_info") hasGeoInfo: Boolean = true,
        @Query("limit")        limit: Int = 200,
        @Query("hidebroken")   hidebroken: Boolean = true
    ): List<StationDto>

    /** Lista de países */
    @GET("json/countries")
    suspend fun getCountries(): List<CountryDto>
}
