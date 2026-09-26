package com.example.iudigitalradio

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class con @HiltAndroidApp — punto de entrada obligatorio para Hilt.
 * Referenciado en AndroidManifest.xml con android:name=".RadioApplication"
 */
@HiltAndroidApp
class RadioApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Configuración requerida por OSMDroid
        org.osmdroid.config.Configuration.getInstance().load(
            this,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        // Set unique User-Agent to avoid API blocks
        org.osmdroid.config.Configuration.getInstance().userAgentValue = "com.example.iudigitalradio/1.0"
    }
}
