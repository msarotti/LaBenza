package com.example.labenza.data.repository

import com.example.labenza.data.model.FavoriteStation

/**
 * Abstraction over favorite-station persistence. The production implementation is
 * [FavoritesRepository] (backed by SharedPreferences).
 */
interface FavoritesDataSource {
    fun load(): List<FavoriteStation>
    fun save(favorites: List<FavoriteStation>)
}
