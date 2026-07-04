package com.example.labenza.data.model

/**
 * A country whose fuel prices the app can show. Only [ITALY] is currently active;
 * the enum exists so the home-screen country selector and its national averages
 * can grow to other European countries later.
 *
 * [avgBenzina] / [avgDiesel] are **static placeholder** national averages (€/l).
 * TODO: replace with a live national-average source (MIMIT weekly bulletin has no
 * clean JSON API yet); the app currently only has the zone-search endpoint.
 */
enum class Country(
    val displayName: String,
    val flag: String,
    val available: Boolean,
    val avgBenzina: Double?,
    val avgDiesel: Double?
) {
    ITALY(
        displayName = "Italia",
        flag = "🇮🇹",
        available = true,
        avgBenzina = 1.789,
        avgDiesel = 1.699
    );

    companion object {
        val default = ITALY

        /** Countries the user can actually pick right now. */
        val selectable: List<Country> = entries.filter { it.available }
    }
}
