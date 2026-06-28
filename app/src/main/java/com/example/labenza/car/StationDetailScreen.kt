package com.example.labenza.car

import android.content.Intent
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.example.labenza.data.model.Station
import com.example.labenza.util.MapNavigation
import java.util.Locale

/**
 * Detail view for a single station, reached by tapping a row in [FuelListScreen].
 * Offers a "Naviga" action that hands the destination to the car's navigation app.
 */
class StationDetailScreen(
    carContext: CarContext,
    private val station: Station
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val paneBuilder = Pane.Builder()

        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Benzina")
                .addText(formatPrice(station.benzinaPrice))
                .build()
        )
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Diesel")
                .addText(formatPrice(station.dieselPrice))
                .build()
        )
        station.distanceKm?.let { km ->
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle("Distanza")
                    .addText("${String.format(Locale.ITALY, "%.1f", km)} km")
                    .build()
            )
        }
        station.address?.takeIf { it.isNotBlank() }?.let { address ->
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle("Indirizzo")
                    .addText(address)
                    .build()
            )
        }

        paneBuilder.addAction(navigateAction())

        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle(station.name ?: station.brand ?: "Distributore")
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun navigateAction(): Action {
        val builder = Action.Builder()
            .setTitle("Naviga")
            .setBackgroundColor(CarColor.GREEN)

        val location = station.location
        builder.setOnClickListener {
            if (location == null) {
                carContext.let {
                    androidx.car.app.CarToast.makeText(
                        it,
                        "Posizione non disponibile.",
                        androidx.car.app.CarToast.LENGTH_SHORT
                    ).show()
                }
                return@setOnClickListener
            }
            val intent = Intent(
                CarContext.ACTION_NAVIGATE,
                MapNavigation.geoUri(location.lat, location.lng, station.name)
            )
            carContext.startCarApp(intent)
        }
        return builder.build()
    }

    private fun formatPrice(price: Double?): String =
        if (price != null) "${String.format(Locale.ITALY, "%.3f", price)} €/l" else "N/A"
}
