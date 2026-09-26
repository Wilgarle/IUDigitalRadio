package com.example.iudigitalradio.domain.repository

import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.domain.model.Country
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio (interfaz de dominio).
 * La capa de presentación NUNCA accede a RadioRepositoryImpl directamente.
 * El dominio no importa clases Android ni de frameworks.
 */
interface RadioRepository {

    /** Top emisoras más populares */
    suspend fun getTopStations(): Result<List<RadioStation>>

    /** Búsqueda por nombre con limit */
    suspend fun searchStations(query: String): Result<List<RadioStation>>

    /** Emisoras por país (código ISO-2) */
    suspend fun getStationsByCountry(countryCode: String): Result<List<RadioStation>>

    /** Emisoras por género/tag */
    suspend fun getStationsByTag(tag: String): Result<List<RadioStation>>

    /** Emisoras con coordenadas para el mapa mundial, con limit configurable */
    suspend fun getStationsForMap(limit: Int = 200): Result<List<RadioStation>>

    /** Emisoras de un país concreto con coordenadas, limitadas */
    suspend fun getStationsByCountryForMap(countryCode: String, limit: Int): Result<List<RadioStation>>

    /** Emisoras de una lista de países con coordenadas, máximo `limit` por país */
    suspend fun getStationsByCountriesForMap(countryCodes: List<String>, limitPerCountry: Int): Result<List<RadioStation>>

    /** Flow reactivo de favoritas desde Room */
    fun getFavorites(): Flow<List<RadioStation>>

    /** Agrega o elimina una emisora de favoritas */
    suspend fun toggleFavorite(station: RadioStation)

    /** Registra un click/escucha en la API */
    suspend fun registerClick(uuid: String)

    /** Obtiene lista de países disponibles */
    suspend fun getCountries(): Result<List<Country>>
}
