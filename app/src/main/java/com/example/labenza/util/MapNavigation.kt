package com.example.labenza.util

import android.net.Uri

/**
 * Builds a vendor-neutral `geo:` URI so any installed map app (Google Maps,
 * OsmAnd, Organic Maps, ...) can open the destination — no Google dependency.
 */
object MapNavigation {
    fun geoUri(lat: Double, lng: Double, label: String?): Uri {
        val name = label?.takeIf { it.isNotBlank() }
        return if (name != null) {
            Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(name)})")
        } else {
            Uri.parse("geo:$lat,$lng?q=$lat,$lng")
        }
    }
}
