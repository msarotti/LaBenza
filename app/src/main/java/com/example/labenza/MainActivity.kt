package com.example.labenza

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.labenza.data.repository.FuelRepository
import com.example.labenza.data.repository.GeocodingRepository
import com.example.labenza.location.LocationHelper
import com.example.labenza.ui.screens.HomeScreen
import com.example.labenza.ui.screens.MainScreen
import com.example.labenza.ui.theme.LaBenzaTheme
import com.example.labenza.ui.viewmodel.FuelViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = FuelRepository()
        val geocodingRepository = GeocodingRepository()
        val locationHelper = LocationHelper(this)
        
        enableEdgeToEdge()
        setContent {
            LaBenzaTheme {
                val viewModel: FuelViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return FuelViewModel(repository, geocodingRepository, locationHelper) as T
                        }
                    }
                )

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                        // Permissions granted
                    }
                }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }

                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(onNavigateToSearch = { navController.navigate("search") })
                    }
                    composable("search") {
                        MainScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
