package com.example.labenza.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.labenza.data.model.FavoriteStation
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persists the user's favorite stations as a JSON list in [SharedPreferences].
 * Kept intentionally simple (no DB dependency) to match the app's data layer.
 */
class FavoritesRepository(context: Context) : FavoritesDataSource {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    override fun load(): List<FavoriteStation> {
        val raw = prefs.getString(KEY_STATIONS, null) ?: return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun save(favorites: List<FavoriteStation>) {
        prefs.edit().putString(KEY_STATIONS, json.encodeToString(favorites)).apply()
    }

    companion object {
        private const val PREFS_NAME = "labenza_favorites"
        private const val KEY_STATIONS = "stations"
    }
}
