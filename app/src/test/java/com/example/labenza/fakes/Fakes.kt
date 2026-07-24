package com.example.labenza.fakes

import com.example.labenza.data.model.FavoriteStation
import com.example.labenza.data.model.PlaceSuggestion
import com.example.labenza.data.model.RegionalAverages
import com.example.labenza.data.model.Station
import com.example.labenza.data.repository.AveragePriceDataSource
import com.example.labenza.data.repository.FavoritesDataSource
import com.example.labenza.data.repository.FuelDataSource
import com.example.labenza.data.repository.GeocodingDataSource
import com.example.labenza.location.LocationProvider

/**
 * Hand-written test doubles for the [FuelViewModel]'s collaborators. Kept simple
 * (no mocking framework) — each records just enough to assert on in tests.
 */
class FakeFuelDataSource : FuelDataSource {
    var result: Result<List<Station>> = Result.success(emptyList())
    var lastRadiusKm: Int? = null
    var callCount = 0

    override suspend fun getNearbyStations(
        lat: Double,
        lng: Double,
        radiusKm: Int
    ): Result<List<Station>> {
        callCount++
        lastRadiusKm = radiusKm
        return result
    }
}

class FakeGeocodingDataSource : GeocodingDataSource {
    var suggestions: List<PlaceSuggestion> = emptyList()
    var reverseLabel: String? = "Posizione di prova"

    override suspend fun autocomplete(query: String): List<PlaceSuggestion> = suggestions
    override suspend fun reverse(lat: Double, lng: Double): String? = reverseLabel
}

class FakeAveragePriceDataSource : AveragePriceDataSource {
    var result: Result<RegionalAverages> =
        Result.success(RegionalAverages(updated = null, regions = emptyList()))

    override suspend fun getRegionalAverages(): Result<RegionalAverages> = result
}

class FakeFavoritesDataSource : FavoritesDataSource {
    var stored: List<FavoriteStation> = emptyList()

    override fun load(): List<FavoriteStation> = stored
    override fun save(favorites: List<FavoriteStation>) {
        stored = favorites
    }
}

class FakeLocationProvider : LocationProvider {
    var coordinates: Pair<Double, Double>? = 45.4642 to 9.19

    override suspend fun getCurrentCoordinates(): Pair<Double, Double>? = coordinates
}
