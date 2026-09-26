package com.example.iudigitalradio.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import com.example.iudigitalradio.data.local.RadioDatabase
import com.example.iudigitalradio.data.local.StationDao
import com.example.iudigitalradio.data.remote.RadioBrowserApiService
import com.example.iudigitalradio.data.repository.RadioRepositoryImpl
import com.example.iudigitalradio.domain.repository.RadioRepository
import com.example.iudigitalradio.domain.repository.UserPreferencesRepository
import com.example.iudigitalradio.data.repository.UserPreferencesRepositoryImpl
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val RADIO_BROWSER_BASE_URL = "https://de1.api.radio-browser.info/"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                // Radio Browser API requiere User-Agent
                val request = chain.request().newBuilder()
                    .header("User-Agent", "IUDigitalRadio/1.0")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create()
        return Retrofit.Builder()
            .baseUrl(RADIO_BROWSER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideRadioBrowserApiService(retrofit: Retrofit): RadioBrowserApiService =
        retrofit.create(RadioBrowserApiService::class.java)

    @Provides
    @Singleton
    fun provideRadioDatabase(@ApplicationContext context: Context): RadioDatabase =
        Room.databaseBuilder(
            context,
            RadioDatabase::class.java,
            "radio_database"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideStationDao(database: RadioDatabase): StationDao = database.stationDao()

    /**
     * ExoPlayer singleton — compartido entre PlayerViewModel y MediaPlaybackService.
     * Release en MediaPlaybackService.onDestroy() (el Service es el propietario del ciclo de vida).
     */
    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer =
        ExoPlayer.Builder(context).build()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRadioRepository(impl: RadioRepositoryImpl): RadioRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
