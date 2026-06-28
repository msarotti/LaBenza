package com.example.labenza.data.api

import com.example.labenza.data.model.ZoneRequest
import com.example.labenza.data.model.ZoneResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Official MIMIT "Osservaprezzi Carburanti" API (same backend used by
 * https://carburanti.mise.gov.it). The zone endpoint returns the fuel stations
 * within [ZoneRequest.radius] km of the given point, with current prices and distance.
 */
interface FuelPriceApi {
    @POST("ospzApi/search/zone")
    suspend fun searchZone(@Body request: ZoneRequest): ZoneResponse

    companion object {
        const val BASE_URL = "https://carburanti.mise.gov.it/"
    }
}
