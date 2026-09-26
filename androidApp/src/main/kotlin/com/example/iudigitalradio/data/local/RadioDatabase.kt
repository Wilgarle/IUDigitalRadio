package com.example.iudigitalradio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room Database — base de datos local para favoritas.
 * Instanciada como Singleton por Hilt en AppModule.
 */
@Database(
    entities = [StationEntity::class],
    version  = 1,
    exportSchema = false
)
abstract class RadioDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao
}
