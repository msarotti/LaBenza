package com.example.labenza.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** A geographic point plus a human-readable label, ready to query the fuel API. */
data class LocationInfo(
    val lat: Double,
    val lng: Double,
    val label: String
)

/**
 * Obtains the device location using the AOSP [LocationManager] only — no Google
 * Play Services — so it works on de-Googled devices.
 */
class LocationHelper(context: Context) : LocationProvider {
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /** Returns (lat, lng) of the current position, or null if unavailable. */
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentCoordinates(): Pair<Double, Double>? {
        lastKnownLocation()?.let { return it.latitude to it.longitude }
        val fresh = withTimeoutOrNull(FIX_TIMEOUT_MS) { requestSingleFix() }
        return fresh?.let { it.latitude to it.longitude }
    }

    @SuppressLint("MissingPermission")
    private fun lastKnownLocation(): Location? =
        PROVIDERS
            .filter { runCatching { locationManager.isProviderEnabled(it) }.getOrDefault(false) }
            .mapNotNull { runCatching { locationManager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleFix(): Location? = withContext(Dispatchers.Main) {
        val provider = PROVIDERS.firstOrNull {
            runCatching { locationManager.isProviderEnabled(it) }.getOrDefault(false)
        } ?: return@withContext null

        suspendCancellableCoroutine { cont ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    if (cont.isActive) cont.resume(location)
                }

                override fun onProviderDisabled(provider: String) {}
                override fun onProviderEnabled(provider: String) {}

                @Deprecated("Deprecated in API 29")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }

            locationManager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            cont.invokeOnCancellation { locationManager.removeUpdates(listener) }
        }
    }

    companion object {
        private val PROVIDERS = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )
        private const val FIX_TIMEOUT_MS = 12_000L
    }
}