package com.example.labenza.data.model

import kotlinx.serialization.Serializable

/**
 * A gas station the user has marked as favorite. Persisted as JSON, so it stores
 * only the stable identity/label fields (not live prices, which go stale).
 */
@Serializable
data class FavoriteStation(
    val id: Long,
    val name: String? = null,
    val brand: String? = null,
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

/** Snapshot a search-result [Station] into a persistable favorite. */
fun Station.toFavorite(): FavoriteStation = FavoriteStation(
    id = id,
    name = name,
    brand = brand,
    address = address,
    lat = location?.lat,
    lng = location?.lng
)
