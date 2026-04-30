package com.example.pekomon.weatherly.core.model

data class Location(
    val id: String,
    val name: String,
    val adminRegion: String?,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
)
