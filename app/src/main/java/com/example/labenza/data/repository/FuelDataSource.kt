package com.example.labenza.data.repository

import com.example.labenza.data.model.Station

/**
 * Abstraction over the fuel-station backend, so the presentation layer can depend
 * on it and tests can substitute a fake. The production implementation is
 * [FuelRepository].
 */
interface FuelDataSource {
    /** Returns the stations within [radiusKm] km of ([lat], [lng]), sorted by distance. */
    suspend fun getNearbyStations(
        lat: Double,
        lng: Double,
        radiusKm: Int = DEFAULT_RADIUS_KM
    ): Result<List<Station>>

    companion object {
        const val DEFAULT_RADIUS_KM = 5
    }
}
