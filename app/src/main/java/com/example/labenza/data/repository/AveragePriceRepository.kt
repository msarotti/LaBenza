package com.example.labenza.data.repository

import com.example.labenza.data.api.AveragePriceApi
import com.example.labenza.data.model.RegionAverages
import com.example.labenza.data.model.RegionalAverages
import com.example.labenza.data.model.RegionalFuel
import retrofit2.Retrofit

class AveragePriceRepository : AveragePriceDataSource {
    private val api = Retrofit.Builder()
        .baseUrl(AveragePriceApi.BASE_URL)
        .build()
        .create(AveragePriceApi::class.java)

    /**
     * Fetches the regional-average CSV and parses it into per-region fuel rows.
     * National figures are derived from these (see [RegionalAverages]).
     */
    override suspend fun getRegionalAverages(): Result<RegionalAverages> {
        return try {
            val csv = api.getRegionalAverages().string()
            Result.success(parse(csv))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parse(csv: String): RegionalAverages {
        val lines = csv.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        val updated = lines.firstOrNull()
            ?.takeIf { it.startsWith("Aggiornamento", ignoreCase = true) }
            ?.removePrefix("Aggiornamento")
            ?.trim()

        // Preserve the CSV's region order.
        val byRegion = LinkedHashMap<String, MutableList<RegionalFuel>>()

        for (line in lines) {
            val cols = line.split(';')
            if (cols.size < 4) continue
            val region = cols[0].trim()
            if (region.isEmpty() || region.equals("REGIONE", ignoreCase = true)) continue
            val type = cols[1].trim()
            val delivery = cols[2].trim()
            val price = cols[3].trim().replace(',', '.').toDoubleOrNull() ?: continue
            byRegion.getOrPut(region) { mutableListOf() }
                .add(RegionalFuel(type = type, delivery = delivery, price = price))
        }

        val regions = byRegion.map { (region, fuels) -> RegionAverages(region, fuels) }
        return RegionalAverages(updated = updated, regions = regions)
    }
}
