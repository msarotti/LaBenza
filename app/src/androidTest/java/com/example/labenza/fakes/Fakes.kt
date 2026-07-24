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

/** Test doubles for driving [FuelViewModel] in Compose UI tests. */
class FakeFuelDataSource(
    var result: Result<List<Station>> = Result.success(emptyList())
) : FuelDataSource {
    override suspend fun getNearbyStations(
        lat: Double,
        lng: Double,
        radiusKm: Int
    ): Result<List<Station>> = result
}

class FakeGeocodingDataSource : GeocodingDataSource {
    override suspend fun autocomplete(query: String): List<PlaceSuggestion> = emptyList()
    override suspend fun reverse(lat: Double, lng: Double): String? = "Posizione di prova"
}

class FakeAveragePriceDataSource : AveragePriceDataSource {
    override suspend fun getRegionalAverages(): Result<RegionalAverages> =
        Result.success(RegionalAverages(updated = null, regions = emptyList()))
}

class FakeFavoritesDataSource : FavoritesDataSource {
    override fun load(): List<FavoriteStation> = emptyList()
    override fun save(favorites: List<FavoriteStation>) {}
}

class FakeLocationProvider : LocationProvider {
    override suspend fun getCurrentCoordinates(): Pair<Double, Double>? = 45.4642 to 9.19
}
