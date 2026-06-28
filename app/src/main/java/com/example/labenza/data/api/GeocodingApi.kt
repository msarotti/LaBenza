package com.example.labenza.data.api

import com.example.labenza.data.model.NominatimPlace
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * OpenStreetMap Nominatim geocoding API. Works on de-Googled devices since it
 * relies on no Google Play services.
 *
 * Nominatim usage policy requires a descriptive User-Agent and at most ~1 req/sec,
 * so callers must debounce the autocomplete input.
 */
interface GeocodingApi {
    @Headers("User-Agent: LaBenza/1.0 (Android fuel price app)")
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 6,
        @Query("countrycodes") countryCodes: String = "it"
    ): List<NominatimPlace>

    @Headers("User-Agent: LaBenza/1.0 (Android fuel price app)")
    @GET("reverse")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("zoom") zoom: Int = 14
    ): NominatimPlace

    companion object {
        const val BASE_URL = "https://nominatim.openstreetmap.org/"
    }
}
