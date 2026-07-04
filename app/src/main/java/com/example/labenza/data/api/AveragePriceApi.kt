package com.example.labenza.data.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers

/**
 * MIMIT published CSV of *regional* road fuel averages ("Media Regionale
 * Stradale"). One row per region and fuel type:
 *   REGIONE;TIPOLOGIA;EROGAZIONE;PREZZO MEDIO
 * The first line is an "Aggiornamento dd-MM-yyyy" date; there is no national row,
 * so the national figure is computed as the mean of the regional values.
 */
interface AveragePriceApi {
    @Headers("User-Agent: LaBenza/1.0 (Android fuel price app)")
    @GET("images/stories/carburanti/MediaRegionaleStradale.csv")
    suspend fun getRegionalAverages(): ResponseBody

    companion object {
        const val BASE_URL = "https://www.mimit.gov.it/"
    }
}
