package com.example.iudigitalradio

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform