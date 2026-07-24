package com.example.labenza.location

/**
 * Abstraction over device-location retrieval, so the presentation layer can depend
 * on it and tests can substitute a fake. The production implementation is
 * [LocationHelper].
 */
interface LocationProvider {
    /** Returns (lat, lng) of the current position, or null if unavailable. */
    suspend fun getCurrentCoordinates(): Pair<Double, Double>?
}
