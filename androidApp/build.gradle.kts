
plugins {
    // androidApplication DEBE ir primero para que Hilt encuentre BaseExtension
    alias(libs.plugins.androidApplication)
    // NOTE: kotlin.android plugin NO es necesario con AGP 9.0+ (Kotlin built-in)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.iudigitalradio"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.iudigitalradio"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared"))

    // ── AndroidX Compose BOM — manages all androidx.compose versions ──────────
    implementation(platform(libs.compose.bom))
    implementation(libs.ax.compose.material3)
    implementation(libs.ax.compose.ui)
    implementation(libs.ax.compose.animation)
    implementation(libs.ax.compose.material.icons)
    implementation(libs.ax.compose.foundation)
    debugImplementation(libs.ax.compose.ui.tooling)
    implementation(libs.ax.compose.ui.tooling.preview)

    // Activity + Compose entry point
    implementation(libs.androidx.activity.compose)

    // ── Hilt — Dependency Injection ──────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    implementation(libs.ax.lifecycle.viewmodel.compose)
    implementation(libs.ax.lifecycle.runtime.compose)
    implementation(libs.ax.lifecycle.viewmodel.ktx)

    // ── Navigation ───────────────────────────────────────────────────────────
    implementation(libs.navigation.compose)

    // ── Media3 ExoPlayer (RF-07) ──────────────────────────────────────────────
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.ui)

    // ── Networking — Retrofit + OkHttp ────────────────────────────────────────
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)

    // ── Room — Local Database (Favorites) ─────────────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ── Coil 3 — Async Image Loading (RF-02) ──────────────────────────────────
    implementation(libs.coil.compose)
    implementation(libs.coil.network)

    // ── Kotlin Coroutines ─────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)

    // ── Kotlinx Serialization (type-safe navigation routes) ──────────────────
    implementation(libs.kotlinx.serialization.json)

    // ── AndroidX Core ────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)

    // ── DataStore ────────────────────────────────────────────────────────────
    implementation(libs.datastore.preferences)

    // ── OSMDroid (Mapas nativos) ─────────────────────────────────────────────
    implementation(libs.osmdroid.android)
    implementation(libs.osmdroid.wms)
}