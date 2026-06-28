package com.example.labenza.data.model

import kotlinx.serialization.Serializable

/**
 * Models for the official "Osservaprezzi Carburanti" (MIMIT) zone-search API:
 *   POST https://carburanti.mise.gov.it/ospzApi/search/zone
 */

@Serializable
data class Point(
    val lat: Double,
    val lng: Double
)

@Serializable
data class ZoneRequest(
    val points: List<Point>,
    val radius: Int,
    // "1-0" = any fuel, any service mode. The response still lists every fuel per station.
    val fuelType: String = "1-0"
)

@Serializable
data class ZoneResponse(
    val success: Boolean = false,
    val center: Point? = null,
    val results: List<Station> = emptyList()
)

@Serializable
data class Station(
    val id: Long,
    val name: String? = null,
    val brand: String? = null,
    val address: String? = null,
    val location: Point? = null,
    // The API returns distance (in km) as a string.
    val distance: String? = null,
    val fuels: List<Fuel> = emptyList()
) {
    val distanceKm: Double? get() = distance?.toDoubleOrNull()

    /** Cheapest benzina price (fuelId 1) across self/served, or null if unavailable. */
    val benzinaPrice: Double? get() = priceFor(FUEL_BENZINA)

    /** Cheapest diesel price (fuelId 2 = Gasolio) across self/served, or null if unavailable. */
    val dieselPrice: Double? get() = priceFor(FUEL_GASOLIO)

    private fun priceFor(fuelId: Int): Double? =
        fuels.filter { it.fuelId == fuelId && it.price != null }
            .mapNotNull { it.price }
            .minOrNull()

    companion object {
        const val FUEL_BENZINA = 1
        const val FUEL_GASOLIO = 2
    }
}

@Serializable
data class Fuel(
    val id: Long = 0,
    val price: Double? = null,
    val name: String? = null,
    val fuelId: Int? = null,
    val isSelf: Boolean = false
)
