package com.example.labenza.data.model

/**
 * Parsed content of MIMIT's regional-average CSV. Holds every region/fuel row and
 * derives the national figures as the mean of the per-region prices.
 */
data class RegionalAverages(
    val updated: String?,
    val regions: List<RegionAverages>
) {
    val nationalBenzina: Double? get() = nationalAverage(TYPE_BENZINA)
    val nationalDiesel: Double? get() = nationalAverage(TYPE_GASOLIO)

    private fun nationalAverage(type: String): Double? {
        val prices = regions.mapNotNull { region ->
            region.fuels.firstOrNull { it.type.equals(type, ignoreCase = true) }?.price
        }
        return if (prices.isEmpty()) null else prices.average()
    }

    companion object {
        const val TYPE_BENZINA = "Benzina"
        const val TYPE_GASOLIO = "Gasolio"
    }
}

/** All fuel averages for a single region. */
data class RegionAverages(
    val region: String,
    val fuels: List<RegionalFuel>
)

/** One fuel row: e.g. Benzina / SELF / 1.823. */
data class RegionalFuel(
    val type: String,      // Benzina, Gasolio, GPL, Metano
    val delivery: String,  // SELF / SERVITO
    val price: Double
)
