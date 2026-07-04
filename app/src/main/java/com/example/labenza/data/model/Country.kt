package com.example.labenza.data.model

/**
 * A country whose fuel prices the app can show. Only [ITALY] is currently active;
 * the enum exists so the home-screen country selector can grow to other European
 * countries later. National-average prices are fetched from a data source (see
 * [com.example.labenza.data.repository.AveragePriceRepository]), not stored here.
 */
enum class Country(
    val displayName: String,
    val flag: String,
    val available: Boolean
) {
    ITALY(displayName = "Italia", flag = "🇮🇹", available = true);

    companion object {
        val default = ITALY

        /** Countries the user can actually pick right now. */
        val selectable: List<Country> = entries.filter { it.available }
    }
}
