package com.example.labenza.data.repository

import com.example.labenza.data.api.GeocodingApi
import com.example.labenza.data.model.NominatimAddress
import com.example.labenza.data.model.NominatimPlace
import com.example.labenza.data.model.PlaceSuggestion
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class GeocodingRepository : GeocodingDataSource {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val api = Retrofit.Builder()
        .baseUrl(GeocodingApi.BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(GeocodingApi::class.java)

    /** Autocomplete suggestions for a typed query. Returns empty list on error. */
    override suspend fun autocomplete(query: String): List<PlaceSuggestion> {
        return try {
            api.search(query).map {
                PlaceSuggestion(
                    label = it.shortLabel(),
                    lat = it.latitude,
                    lng = it.longitude
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Human-readable label for a coordinate, or null on error. */
    override suspend fun reverse(lat: Double, lng: Double): String? {
        return try {
            api.reverse(lat, lng).cityLabel()
        } catch (e: Exception) {
            null
        }
    }

    private fun NominatimPlace.cityLabel(): String {
        val city = address?.cityName()
        val province = address?.county ?: address?.state
        return when {
            city != null && province != null -> "$city ($province)"
            city != null -> city
            else -> display_name.substringBefore(",").ifBlank { display_name }
        }
    }

    /** A concise label: the most specific name plus city, avoiding the full address tail. */
    private fun NominatimPlace.shortLabel(): String {
        val parts = display_name.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0]}, ${parts[1]}"
            parts.isNotEmpty() -> parts[0]
            else -> display_name
        }
    }

    private fun NominatimAddress.cityName(): String? =
        city ?: town ?: village ?: municipality
}
