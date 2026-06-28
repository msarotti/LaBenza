package com.example.labenza.car

import android.Manifest
import android.content.pm.PackageManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.constraints.ConstraintManager
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarLocation
import androidx.car.app.model.ItemList
import androidx.car.app.model.Metadata
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Place
import androidx.car.app.model.PlaceListMapTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.labenza.data.model.Station
import com.example.labenza.data.repository.FuelRepository
import com.example.labenza.location.LocationHelper
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Android Auto screen: on launch it grabs the device location automatically and
 * shows the nearby fuel stations as a list with map markers.
 */
class FuelListScreen(carContext: CarContext) : Screen(carContext) {

    private val fuelRepository = FuelRepository()
    private val locationHelper = LocationHelper(carContext)

    private sealed class State {
        object Loading : State()
        object NeedPermission : State()
        data class Error(val message: String) : State()
        data class Success(val stations: List<Station>) : State()
    }

    private var state: State = State.Loading

    init {
        loadData()
    }

    private fun loadData() {
        state = State.Loading
        invalidate()

        if (!hasLocationPermission()) {
            state = State.NeedPermission
            invalidate()
            return
        }

        lifecycleScope.launch {
            val coords = locationHelper.getCurrentCoordinates()
            if (coords == null) {
                state = State.Error("Impossibile determinare la posizione.")
                invalidate()
                return@launch
            }
            fuelRepository.getNearbyStations(coords.first, coords.second)
                .onSuccess {
                    state = State.Success(it)
                    invalidate()
                }
                .onFailure {
                    state = State.Error(it.message ?: "Errore di rete.")
                    invalidate()
                }
        }
    }

    override fun onGetTemplate(): Template = when (val s = state) {
        is State.Loading -> PlaceListMapTemplate.Builder()
            .setTitle(TITLE)
            .setHeaderAction(Action.APP_ICON)
            .setLoading(true)
            .build()

        is State.NeedPermission -> messageTemplate(
            message = "Concedi l'accesso alla posizione per trovare i distributori vicini.",
            actionTitle = "Concedi"
        ) { requestLocationPermission() }

        is State.Error -> messageTemplate(
            message = s.message,
            actionTitle = "Riprova"
        ) { loadData() }

        is State.Success -> PlaceListMapTemplate.Builder()
            .setTitle(TITLE)
            .setHeaderAction(Action.APP_ICON)
            .setCurrentLocationEnabled(true)
            .setItemList(buildItemList(s.stations))
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle("Aggiorna")
                            .setOnClickListener { loadData() }
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun buildItemList(stations: List<Station>): ItemList {
        if (stations.isEmpty()) {
            return ItemList.Builder()
                .setNoItemsMessage("Nessun distributore trovato nelle vicinanze.")
                .build()
        }

        val limit = carContext.getCarService(ConstraintManager::class.java)
            .getContentLimit(ConstraintManager.CONTENT_LIMIT_TYPE_PLACE_LIST)

        val builder = ItemList.Builder()
        stations.take(limit).forEach { station ->
            val row = Row.Builder()
                .setTitle(station.name ?: station.brand ?: "Distributore")
                .addText(priceLine(station))
            station.location?.let {
                row.setMetadata(
                    Metadata.Builder()
                        .setPlace(
                            Place.Builder(CarLocation.create(it.lat, it.lng)).build()
                        )
                        .build()
                )
            }
            builder.addItem(row.build())
        }
        return builder.build()
    }

    private fun priceLine(station: Station): String {
        val benzina = "Benzina ${formatPrice(station.benzinaPrice)}"
        val diesel = "Diesel ${formatPrice(station.dieselPrice)}"
        val distance = station.distanceKm?.let { " · ${String.format(Locale.ITALY, "%.1f", it)} km" } ?: ""
        return "$benzina  ·  $diesel$distance"
    }

    private fun formatPrice(price: Double?): String =
        if (price != null) "${String.format(Locale.ITALY, "%.3f", price)}€" else "N/A"

    private fun messageTemplate(
        message: String,
        actionTitle: String,
        onClick: () -> Unit
    ): Template = MessageTemplate.Builder(message)
        .setTitle(TITLE)
        .setHeaderAction(Action.APP_ICON)
        .addAction(
            Action.Builder()
                .setTitle(actionTitle)
                .setOnClickListener { onClick() }
                .build()
        )
        .build()

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(carContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(carContext, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        val permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        carContext.requestPermissions(permissions) { granted, _ ->
            if (granted.isNotEmpty()) {
                loadData()
            }
        }
    }

    companion object {
        private const val TITLE = "LaBenza - Distributori"
    }
}
