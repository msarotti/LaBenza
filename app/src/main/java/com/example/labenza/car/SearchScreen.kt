package com.example.labenza.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import com.example.labenza.data.repository.GeocodingRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SearchScreen(
    carContext: CarContext,
    private val onLocationSelected: (Double, Double, String) -> Unit
) : Screen(carContext) {

    private val geocodingRepository = GeocodingRepository()
    private var suggestions: List<com.example.labenza.data.model.PlaceSuggestion> = emptyList()

    override fun onGetTemplate(): Template {
        val searchCallback = object : SearchTemplate.SearchCallback {
            override fun onSearchTextChanged(searchText: String) {
                lifecycleScope.launch {
                    val result = geocodingRepository.autocomplete(searchText)
                    suggestions = result
                    invalidate()
                }
            }

            override fun onSearchSubmitted(searchText: String) {
                // Handle submission if needed
            }
        }

        val listBuilder = ItemList.Builder()
        suggestions.forEach { suggestion ->
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(suggestion.label)
                    .setOnClickListener {
                        onLocationSelected(suggestion.lat, suggestion.lng, suggestion.label)
                        screenManager.pop()
                    }
                    .build()
            )
        }

        return SearchTemplate.Builder(searchCallback)
            .setHeaderAction(Action.BACK)
            .setInitialSearchText("")
            .setItemList(listBuilder.build())
            .build()
    }
}
