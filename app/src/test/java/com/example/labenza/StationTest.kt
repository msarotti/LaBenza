package com.example.labenza

import com.example.labenza.data.model.Fuel
import com.example.labenza.data.model.Station
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for the pure computed properties on [Station] — the logic the
 * price/distance sort options rely on.
 */
class StationTest {

    @Test
    fun distanceKm_parsesNumericString() {
        assertEquals(3.4, Station(id = 1, distance = "3.4").distanceKm!!, 1e-9)
    }

    @Test
    fun distanceKm_isNull_whenMissing() {
        assertNull(Station(id = 1, distance = null).distanceKm)
    }

    @Test
    fun distanceKm_isNull_whenNotANumber() {
        assertNull(Station(id = 1, distance = "n/a").distanceKm)
    }

    @Test
    fun benzinaPrice_returnsCheapestBenzinaAcrossServiceModes() {
        val station = Station(
            id = 1,
            fuels = listOf(
                Fuel(fuelId = Station.FUEL_BENZINA, price = 1.959, isSelf = false),
                Fuel(fuelId = Station.FUEL_BENZINA, price = 1.899, isSelf = true),
                Fuel(fuelId = Station.FUEL_GASOLIO, price = 1.799, isSelf = true)
            )
        )
        assertEquals(1.899, station.benzinaPrice!!, 1e-9)
    }

    @Test
    fun dieselPrice_returnsCheapestGasolio() {
        val station = Station(
            id = 1,
            fuels = listOf(
                Fuel(fuelId = Station.FUEL_GASOLIO, price = 1.849),
                Fuel(fuelId = Station.FUEL_GASOLIO, price = 1.799)
            )
        )
        assertEquals(1.799, station.dieselPrice!!, 1e-9)
    }

    @Test
    fun prices_areNull_whenFuelAbsent() {
        val station = Station(
            id = 1,
            fuels = listOf(Fuel(fuelId = Station.FUEL_BENZINA, price = null))
        )
        assertNull(station.benzinaPrice)
        assertNull(station.dieselPrice)
    }
}
