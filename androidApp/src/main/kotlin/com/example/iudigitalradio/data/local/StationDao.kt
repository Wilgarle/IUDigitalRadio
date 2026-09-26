package com.example.iudigitalradio.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO para las emisoras favoritas.
 * getFavorites() retorna Flow para observación reactiva desde ViewModel.
 */
@Dao
interface StationDao {

    /** Observa todas las favoritas en tiempo real (Flow reactivo) */
    @Query("SELECT * FROM favorite_stations ORDER BY name ASC")
    fun getAllFavorites(): Flow<List<StationEntity>>

    /** Inserta o actualiza una favorita */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(station: StationEntity)

    /** Elimina una favorita por entidad */
    @Delete
    suspend fun deleteFavorite(station: StationEntity)

    /** Elimina una favorita por UUID */
    @Query("DELETE FROM favorite_stations WHERE stationuuid = :uuid")
    suspend fun deleteFavoriteByUuid(uuid: String)

    /** Verifica si una emisora ya es favorita */
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE stationuuid = :uuid)")
    suspend fun isFavorite(uuid: String): Boolean

    /** Cuenta total de favoritas */
    @Query("SELECT COUNT(*) FROM favorite_stations")
    fun getFavoritesCount(): Flow<Int>
}
