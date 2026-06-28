package com.example.labenza.data.repository

import com.example.labenza.data.api.FuelPriceApi
import com.example.labenza.data.model.Point
import com.example.labenza.data.model.Station
import com.example.labenza.data.model.ZoneRequest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class FuelRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val api = Retrofit.Builder()
        .baseUrl(FuelPriceApi.BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(FuelPriceApi::class.java)

    /**
     * Returns the stations within [radiusKm] km of ([lat], [lng]), sorted by distance.
     */
    suspend fun getNearbyStations(
        lat: Double,
        lng: Double,
        radiusKm: Int = DEFAULT_RADIUS_KM
    ): Result<List<Station>> {
        return try {
            val response = api.searchZone(
                ZoneRequest(points = listOf(Point(lat, lng)), radius = radiusKm)
            )
            if (!response.success) {
                Result.failure(Exception("Nessun risultato dal servizio Osservaprezzi."))
            } else {
                Result.success(response.results.sortedBy { it.distanceKm ?: Double.MAX_VALUE })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val DEFAULT_RADIUS_KM = 5
    }
}
