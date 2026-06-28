package com.example.labenza.data.model

import kotlinx.serialization.Serializable

/**
 * Models for the OpenStreetMap Nominatim geocoder (no Google services required):
 *   https://nominatim.openstreetmap.org/search   — forward / autocomplete
 *   https://nominatim.openstreetmap.org/reverse   — reverse (coords -> place)
 */
@Serializable
data class NominatimPlace(
    val place_id: Long = 0,
    val lat: String = "0",
    val lon: String = "0",
    val display_name: String = "",
    val name: String? = null,
    val address: NominatimAddress? = null
) {
    val latitude: Double get() = lat.toDoubleOrNull() ?: 0.0
    val longitude: Double get() = lon.toDoubleOrNull() ?: 0.0
}

@Serializable
data class NominatimAddress(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val municipality: String? = null,
    val county: String? = null,
    val state: String? = null,
    val road: String? = null,
    val house_number: String? = null
)

/** Domain object used by the UI for an autocomplete suggestion. */
data class PlaceSuggestion(
    val label: String,
    val lat: Double,
    val lng: Double
)
