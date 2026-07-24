package com.example.labenza

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.labenza.fakes.FakeAveragePriceDataSource
import com.example.labenza.fakes.FakeFavoritesDataSource
import com.example.labenza.fakes.FakeFuelDataSource
import com.example.labenza.fakes.FakeGeocodingDataSource
import com.example.labenza.fakes.FakeLocationProvider
import com.example.labenza.ui.screens.MainScreen
import com.example.labenza.ui.theme.LaBenzaTheme
import com.example.labenza.ui.viewmodel.FuelViewModel
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the phone search screen: verifies the sort chips
 * ("Min"/"Max"/"Distanza") and the search-radius slider render and respond.
 */
class MainScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setUpScreen() {
        val viewModel = FuelViewModel(
            FakeFuelDataSource(),
            FakeGeocodingDataSource(),
            FakeAveragePriceDataSource(),
            FakeFavoritesDataSource(),
            FakeLocationProvider()
        )
        composeRule.setContent {
            LaBenzaTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    @Test
    fun sortChips_areDisplayed() {
        setUpScreen()
        composeRule.onNodeWithText("Min").assertIsDisplayed()
        composeRule.onNodeWithText("Max").assertIsDisplayed()
        composeRule.onNodeWithText("Distanza").assertIsDisplayed()
    }

    @Test
    fun tappingDistanceChip_selectsIt() {
        setUpScreen()
        composeRule.onNodeWithText("Distanza").performClick()
        composeRule.onNodeWithText("Distanza").assertIsSelected()
    }

    @Test
    fun radiusSlider_showsLabelAndDefaultValue() {
        setUpScreen()
        composeRule.onNodeWithText("Raggio di ricerca").assertIsDisplayed()
        // Default radius is FuelDataSource.DEFAULT_RADIUS_KM (5 km).
        composeRule.onNodeWithText("5 km").assertIsDisplayed()
    }
}
