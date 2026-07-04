package com.example.labenza.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.labenza.data.model.RegionalAverages
import com.example.labenza.data.model.PlaceSuggestion
import com.example.labenza.data.model.Station
import com.example.labenza.data.model.SortOrder
import com.example.labenza.data.repository.AveragePriceRepository
import com.example.labenza.data.repository.FuelRepository
import com.example.labenza.data.repository.GeocodingRepository
import com.example.labenza.location.LocationHelper
import com.example.labenza.location.LocationInfo
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

data class FuelResult(
    val location: LocationInfo,
    val stations: List<Station>,
    val avgBenzina: Double?,
    val avgDiesel: Double?
)

sealed class FuelUiState {
    object Idle : FuelUiState()
    object Loading : FuelUiState()
    data class Success(val result: FuelResult) : FuelUiState()
    data class Error(val message: String) : FuelUiState()
}

sealed class AverageUiState {
    object Loading : AverageUiState()
    data class Success(val data: RegionalAverages) : AverageUiState()
    object Error : AverageUiState()
}

class FuelViewModel(
    private val fuelRepository: FuelRepository,
    private val geocodingRepository: GeocodingRepository,
    private val averagePriceRepository: AveragePriceRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<FuelUiState>(FuelUiState.Idle)
    val uiState: StateFlow<FuelUiState> = _uiState.asStateFlow()

    private val _averages = MutableStateFlow<AverageUiState>(AverageUiState.Loading)
    val averages: StateFlow<AverageUiState> = _averages.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _suggestions = MutableStateFlow<List<PlaceSuggestion>>(emptyList())
    val suggestions: StateFlow<List<PlaceSuggestion>> = _suggestions.asStateFlow()

    private val _sortOrder = MutableStateFlow<SortOrder>(SortOrder.NONE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Set to true right after a suggestion is picked, to suppress re-querying that text.
    private var suppressNextQuery = false

    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        viewModelScope.launch {
            _query
                .debounce(AUTOCOMPLETE_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { q ->
                    if (suppressNextQuery) {
                        suppressNextQuery = false
                        return@collectLatest
                    }
                    _suggestions.value = if (q.trim().length < MIN_QUERY_LENGTH) {
                        emptyList()
                    } else {
                        geocodingRepository.autocomplete(q.trim())
                    }
                }
        }
    }

    init {
        observeQuery()
        loadAverages()
    }

    fun loadAverages() {
        viewModelScope.launch {
            _averages.value = AverageUiState.Loading
            averagePriceRepository.getRegionalAverages()
                .onSuccess { _averages.value = AverageUiState.Success(it) }
                .onFailure { _averages.value = AverageUiState.Error }
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        // Re-apply sorting to the current state if it's Success
        val currentState = _uiState.value
        if (currentState is FuelUiState.Success) {
            val stations = currentState.result.stations
            val sorted = sortStations(stations, order)
            _uiState.value = FuelUiState.Success(currentState.result.copy(stations = sorted))
        }
    }

    private fun sortStations(stations: List<Station>, order: SortOrder): List<Station> {
        return when (order) {
            SortOrder.PRICE_LOW_TO_HIGH -> stations.sortedBy { it.benzinaPrice ?: Double.MAX_VALUE }
            SortOrder.PRICE_HIGH_TO_LOW -> stations.sortedByDescending { it.benzinaPrice ?: Double.MIN_VALUE }
            else -> stations
        }
    }

    fun onQueryChange(text: String) {
        _query.value = text
    }

    fun selectSuggestion(suggestion: PlaceSuggestion) {
        suppressNextQuery = true
        _query.value = suggestion.label
        _suggestions.value = emptyList()
        viewModelScope.launch {
            _uiState.value = FuelUiState.Loading
            fetchPrices(LocationInfo(suggestion.lat, suggestion.lng, suggestion.label))
        }
    }

    fun fetchByLocation() {
        viewModelScope.launch {
            _uiState.value = FuelUiState.Loading
            _suggestions.value = emptyList()
            val coords = locationHelper.getCurrentCoordinates()
            if (coords == null) {
                _uiState.value = FuelUiState.Error(
                    "Impossibile determinare la posizione attuale. Verifica i permessi e il GPS."
                )
                return@launch
            }
            val (lat, lng) = coords
            val label = geocodingRepository.reverse(lat, lng) ?: "Posizione attuale"
            fetchPrices(LocationInfo(lat, lng, label))
        }
    }

    private suspend fun fetchPrices(location: LocationInfo) {
        fuelRepository.getNearbyStations(location.lat, location.lng)
            .onSuccess { stations ->
                val sortedStations = sortStations(stations, _sortOrder.value)

                _uiState.value = FuelUiState.Success(
                    FuelResult(
                        location = location,
                        stations = sortedStations,
                        avgBenzina = stations.mapNotNull { it.benzinaPrice }.averageOrNull(),
                        avgDiesel = stations.mapNotNull { it.dieselPrice }.averageOrNull()
                    )
                )
            }
            .onFailure { error ->
                _uiState.value = FuelUiState.Error(error.message ?: "Errore sconosciuto.")
            }
    }

    private fun List<Double>.averageOrNull(): Double? =
        if (isEmpty()) null else average()

    companion object {
        private const val AUTOCOMPLETE_DEBOUNCE_MS = 400L
        private const val MIN_QUERY_LENGTH = 3
    }
}
