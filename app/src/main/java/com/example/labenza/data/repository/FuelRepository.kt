package com.example.labenza.data.repository

import com.example.labenza.data.api.FuelPriceApi
import com.example.labenza.data.model.Point
import com.example.labenza.data.model.Station
import com.example.labenza.data.model.ZoneRequest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Talks to the official MIMIT "Osservaprezzi Carburanti" backend. [baseUrl] is
 * overridable so tests can point it at a local mock server.
 */
class FuelRepository(
    baseUrl: String = FuelPriceApi.BASE_URL
) : FuelDataSource {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(FuelPriceApi::class.java)

    override suspend fun getNearbyStations(
        lat: Double,
        lng: Double,
        radiusKm: Int
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
}
