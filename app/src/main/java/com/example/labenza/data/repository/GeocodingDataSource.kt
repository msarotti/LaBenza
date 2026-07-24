package com.example.labenza.data.repository

import com.example.labenza.data.model.PlaceSuggestion

/**
 * Abstraction over geocoding (place search + reverse geocoding). The production
 * implementation is [GeocodingRepository].
 */
interface GeocodingDataSource {
    /** Autocomplete suggestions for a typed query. Returns empty list on error. */
    suspend fun autocomplete(query: String): List<PlaceSuggestion>

    /** Human-readable label for a coordinate, or null on error. */
    suspend fun reverse(lat: Double, lng: Double): String?
}
