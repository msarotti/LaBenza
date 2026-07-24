package com.example.labenza

import com.example.labenza.data.model.Fuel
import com.example.labenza.data.model.PlaceSuggestion
import com.example.labenza.data.model.SortOrder
import com.example.labenza.data.model.Station
import com.example.labenza.data.repository.FuelDataSource
import com.example.labenza.fakes.FakeAveragePriceDataSource
import com.example.labenza.fakes.FakeFavoritesDataSource
import com.example.labenza.fakes.FakeFuelDataSource
import com.example.labenza.fakes.FakeGeocodingDataSource
import com.example.labenza.fakes.FakeLocationProvider
import com.example.labenza.ui.viewmodel.FuelUiState
import com.example.labenza.ui.viewmodel.FuelViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FuelViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fuel: FakeFuelDataSource
    private lateinit var geocoding: FakeGeocodingDataSource
    private lateinit var averages: FakeAveragePriceDataSource
    private lateinit var favorites: FakeFavoritesDataSource
    private lateinit var location: FakeLocationProvider

    // s1: farthest, mid price | s2: nearest, cheapest | s3: middle distance, dearest.
    private val s1 = station(id = 1, distance = "3.0", benzina = 1.90)
    private val s2 = station(id = 2, distance = "1.0", benzina = 1.80)
    private val s3 = station(id = 3, distance = "2.0", benzina = 2.00)

    @Before
    fun setUp() {
        fuel = FakeFuelDataSource().apply { result = Result.success(listOf(s1, s2, s3)) }
        geocoding = FakeGeocodingDataSource()
        averages = FakeAveragePriceDataSource()
        favorites = FakeFavoritesDataSource()
        location = FakeLocationProvider()
    }

    private fun createViewModel() =
        FuelViewModel(fuel, geocoding, averages, favorites, location)

    private fun station(id: Long, distance: String?, benzina: Double?): Station =
        Station(
            id = id,
            distance = distance,
            fuels = listOfNotNull(
                benzina?.let { Fuel(fuelId = Station.FUEL_BENZINA, price = it) }
            )
        )

    private fun stationIds(vm: FuelViewModel): List<Long> {
        val state = vm.uiState.value
        assertTrue("Expected Success but was $state", state is FuelUiState.Success)
        return (state as FuelUiState.Success).result.stations.map { it.id }
    }

    // --- Search radius -------------------------------------------------------

    @Test
    fun initialRadius_isDefault() {
        val vm = createViewModel()
        assertEquals(FuelDataSource.DEFAULT_RADIUS_KM, vm.searchRadiusKm.value)
    }

    @Test
    fun onRadiusChange_updatesRadiusState() {
        val vm = createViewModel()
        vm.onRadiusChange(15)
        assertEquals(15, vm.searchRadiusKm.value)
    }

    @Test
    fun onRadiusChangeFinished_withoutPriorSearch_doesNotQuery() {
        val vm = createViewModel()
        vm.onRadiusChange(15)
        vm.onRadiusChangeFinished()

        assertEquals(0, fuel.callCount)
        assertTrue(vm.uiState.value is FuelUiState.Idle)
    }

    @Test
    fun search_passesCurrentRadiusToRepository() {
        val vm = createViewModel()
        vm.onRadiusChange(8)
        vm.searchNearby(45.0, 9.0, "Milano")

        assertEquals(8, fuel.lastRadiusKm)
    }

    @Test
    fun onRadiusChangeFinished_afterSearch_reRunsWithNewRadius() {
        val vm = createViewModel()
        vm.searchNearby(45.0, 9.0, "Milano")
        val callsAfterFirstSearch = fuel.callCount

        vm.onRadiusChange(20)
        vm.onRadiusChangeFinished()

        assertEquals(20, fuel.lastRadiusKm)
        assertEquals(callsAfterFirstSearch + 1, fuel.callCount)
    }

    // --- Sorting -------------------------------------------------------------

    @Test
    fun setSortOrder_priceLowToHigh_sortsAscendingByBenzina() {
        val vm = createViewModel()
        vm.searchNearby(45.0, 9.0, "Milano")

        vm.setSortOrder(SortOrder.PRICE_LOW_TO_HIGH)

        assertEquals(listOf(2L, 1L, 3L), stationIds(vm))
    }

    @Test
    fun setSortOrder_priceHighToLow_sortsDescendingByBenzina() {
        val vm = createViewModel()
        vm.searchNearby(45.0, 9.0, "Milano")

        vm.setSortOrder(SortOrder.PRICE_HIGH_TO_LOW)

        assertEquals(listOf(3L, 1L, 2L), stationIds(vm))
    }

    @Test
    fun setSortOrder_distance_sortsAscendingByDistance() {
        val vm = createViewModel()
        vm.searchNearby(45.0, 9.0, "Milano")

        vm.setSortOrder(SortOrder.DISTANCE)

        assertEquals(listOf(2L, 3L, 1L), stationIds(vm))
    }

    @Test
    fun selectSuggestion_appliesCurrentSortOrder() {
        val vm = createViewModel()
        vm.setSortOrder(SortOrder.DISTANCE)

        vm.selectSuggestion(PlaceSuggestion(label = "Roma", lat = 41.9, lng = 12.5))

        assertEquals(listOf(2L, 3L, 1L), stationIds(vm))
    }

    @Test
    fun search_computesFuelAverages() {
        val vm = createViewModel()
        vm.searchNearby(45.0, 9.0, "Milano")

        val result = (vm.uiState.value as FuelUiState.Success).result
        // Mean of 1.90, 1.80, 2.00.
        assertEquals(1.90, result.avgBenzina!!, 1e-9)
    }
}
