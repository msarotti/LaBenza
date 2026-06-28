package com.example.labenza.car

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Entry point for Android Auto (projected). Declared as a POI category app so it
 * can list nearby fuel stations on the car screen.
 */
class FuelCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator {
        // For production you should validate against a known allow-list of hosts.
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session = object : Session() {
        override fun onCreateScreen(intent: Intent): Screen = FuelListScreen(carContext)
    }
}
