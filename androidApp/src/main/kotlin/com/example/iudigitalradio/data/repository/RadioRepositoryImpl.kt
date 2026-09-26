package com.example.iudigitalradio.data.repository

import com.example.iudigitalradio.data.local.StationDao
import com.example.iudigitalradio.data.local.toDomain
import com.example.iudigitalradio.data.local.toEntity
import com.example.iudigitalradio.data.remote.RadioBrowserApiService
import com.example.iudigitalradio.domain.model.RadioStation
import com.example.iudigitalradio.domain.model.Country
import com.example.iudigitalradio.domain.repository.RadioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementación del repositorio — capa de datos.
 * Fuente única de verdad: API remota + cache Room para favoritas.
 * Usa Kotlin Coroutines (Dispatchers.IO) — NUNCA AsyncTask.
 */
class RadioRepositoryImpl @Inject constructor(
    private val apiService: RadioBrowserApiService,
    private val stationDao: StationDao
) : RadioRepository {

    override suspend fun getTopStations(): Result<List<RadioStation>> =
        safeApiCall {
            apiService.getTopStations(50)
                .filter { it.urlResolved.isNotBlank() || it.url.isNotBlank() }
                .map { dto ->
                    val isFav = stationDao.isFavorite(dto.stationuuid)
                    dto.toDomain(isFav)
                }
        }

    override suspend fun searchStations(query: String): Result<List<RadioStation>> =
        safeApiCall {
            apiService.searchStations(name = query)
                .filter { it.urlResolved.isNotBlank() || it.url.isNotBlank() }
                .map { dto ->
                    val isFav = stationDao.isFavorite(dto.stationuuid)
                    dto.toDomain(isFav)
                }
        }

    override suspend fun getStationsByCountry(countryCode: String): Result<List<RadioStation>> =
        safeApiCall {
            apiService.getStationsByCountry(countryCode)
                .filter { it.urlResolved.isNotBlank() || it.url.isNotBlank() }
                .map { it.toDomain() }
        }

    override suspend fun getStationsByTag(tag: String): Result<List<RadioStation>> =
        safeApiCall {
            apiService.getStationsByTag(tag)
                .filter { it.urlResolved.isNotBlank() || it.url.isNotBlank() }
                .map { it.toDomain() }
        }

    override suspend fun getStationsForMap(limit: Int): Result<List<RadioStation>> =
        safeApiCall {
            apiService.getStationsWithCoordinates(limit = limit)
                .filter { it.latitude != null && it.longitude != null }
                .filter { it.urlResolved.isNotBlank() || it.url.isNotBlank() }
                .map { it.toDomain() }
        }

    override suspend fun getStationsByCountryForMap(countryCode: String, limit: Int): Result<List<RadioStation>> =
        safeApiCall {
            apiService.getStationsWithCoordinates(countryCode = countryCode, limit = limit)
                .filter { it.latitude != null && it.longitude != null }
                .filter { it.urlResolved.isNotBlank() || it.url.isNotBlank() }
                .map { it.toDomain() }
        }

    override suspend fun getStationsByCountriesForMap(countryCodes: List<String>, limitPerCountry: Int): Result<List<RadioStation>> =
        safeApiCall {
            countryCodes.flatMap { code ->
                runCatching {
                    apiService.getStationsWithCoordinates(countryCode = code, limit = limitPerCountry)
                        .filter { it.latitude != null && it.longitude != null }
                        .filter { it.urlResolved.isNotBlank() || it.url.isNotBlank() }
                        .map { it.toDomain() }
                }.getOrElse { emptyList() }
            }
        }

    override fun getFavorites(): Flow<List<RadioStation>> =
        stationDao.getAllFavorites().map { entities -> entities.map { it.toDomain() } }

    override suspend fun toggleFavorite(station: RadioStation) {
        withContext(Dispatchers.IO) {
            if (stationDao.isFavorite(station.stationuuid)) {
                stationDao.deleteFavoriteByUuid(station.stationuuid)
            } else {
                stationDao.insertFavorite(station.toEntity())
            }
        }
    }

    override suspend fun registerClick(uuid: String) {
        runCatching { apiService.voteStation(uuid) } // fire-and-forget, ignora errores
    }

    override suspend fun getCountries(): Result<List<Country>> =
        safeApiCall {
            apiService.getCountries().map { 
                Country(name = it.name, isoCode = it.isoCode ?: "", stationCount = it.stationCount ?: 0)
            }
        }

    /**
     * Wrapper de llamadas API — ejecuta en Dispatchers.IO y envuelve errores en Result.
     */
    private suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> =
        withContext(Dispatchers.IO) {
            runCatching { block() }
        }
}
