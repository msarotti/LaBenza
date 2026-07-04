package com.example.labenza.ui.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import com.example.labenza.data.model.SortOrder

class SortOptionScreen(
    carContext: CarContext,
    private val onSortSelected: (SortOrder) -> Unit
) : Screen(carContext) {

    override fun onGetTemplate(): ListTemplate {
        val listBuilder = ItemList.Builder()

        listBuilder.addItem(
            Row.Builder()
                .setTitle("Prezzo: Basso → Alto")
                .setOnClickListener {
                    onSortSelected(SortOrder.PRICE_LOW_TO_HIGH)
                    screenManager.pop()
                }
                .build()
        )

        listBuilder.addItem(
            Row.Builder()
                .setTitle("Prezzo: Alto → Basso")
                .setOnClickListener {
                    onSortSelected(SortOrder.PRICE_HIGH_TO_LOW)
                    screenManager.pop()
                }
                .build()
        )

        listBuilder.addItem(
            Row.Builder()
                .setTitle("Nessun ordine")
                .setOnClickListener {
                    onSortSelected(SortOrder.NONE)
                    screenManager.pop()
                }
                .build()
        )

        return ListTemplate.Builder()
            .setTitle("Ordina per")
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }
}
