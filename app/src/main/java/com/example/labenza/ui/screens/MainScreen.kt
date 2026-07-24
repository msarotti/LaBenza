package com.example.labenza.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.labenza.data.model.PlaceSuggestion
import com.example.labenza.data.model.Station
import com.example.labenza.data.model.SortOrder
import com.example.labenza.ui.viewmodel.FuelResult
import com.example.labenza.ui.viewmodel.FuelUiState
import com.example.labenza.ui.viewmodel.FuelViewModel
import com.example.labenza.util.MapNavigation
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: FuelViewModel,
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val favoriteIds = favorites.mapTo(mutableSetOf()) { it.id }

    // Scroll state of the results list, hoisted so the "use my location" button can
    // hide once the user starts scrolling through the results.
    val listState = rememberLazyListState()
    val successResult = (uiState as? FuelUiState.Success)?.result
    // Reset scroll (and thus re-show the button) whenever a new search comes in.
    LaunchedEffect(successResult?.location?.label) {
        listState.scrollToItem(0)
    }
    val locationButtonVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalGasStation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LaBenza", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Indietro"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Cerca per città o indirizzo") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Cancella")
                        }
                    }
                }
            )

            if (suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                SuggestionsList(suggestions = suggestions, onSelect = viewModel::selectSuggestion)
            }

            Spacer(modifier = Modifier.height(10.dp))

            SortOptions(
                currentOrder = viewModel.sortOrder.collectAsState().value,
                onOrderChange = viewModel::setSortOrder
            )

            Spacer(modifier = Modifier.height(10.dp))

            SearchRadiusSlider(
                radiusKm = viewModel.searchRadiusKm.collectAsState().value,
                onRadiusChange = viewModel::onRadiusChange,
                onRadiusChangeFinished = viewModel::onRadiusChangeFinished
            )

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(
                visible = locationButtonVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    FilledTonalButton(
                        onClick = { viewModel.fetchByLocation() },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Usa la mia posizione", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            when (val state = uiState) {
                is FuelUiState.Idle -> CenterMessage(
                    icon = Icons.Default.LocalGasStation,
                    text = "Inserisci un indirizzo o usa la posizione GPS per trovare i distributori più vicini."
                )
                is FuelUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                is FuelUiState.Success -> ResultsList(
                    result = state.result,
                    listState = listState,
                    favoriteIds = favoriteIds,
                    onToggleFavorite = viewModel::toggleFavorite
                )
                is FuelUiState.Error -> CenterMessage(
                    icon = Icons.Default.Place,
                    text = state.message,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SortOptions(
    currentOrder: SortOrder,
    onOrderChange: (SortOrder) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentOrder == SortOrder.PRICE_LOW_TO_HIGH,
            onClick = {
                onOrderChange(if (currentOrder == SortOrder.PRICE_LOW_TO_HIGH) SortOrder.NONE else SortOrder.PRICE_LOW_TO_HIGH)
            },
            label = { Text("Min") }
        )
        FilterChip(
            selected = currentOrder == SortOrder.PRICE_HIGH_TO_LOW,
            onClick = {
                onOrderChange(if (currentOrder == SortOrder.PRICE_HIGH_TO_LOW) SortOrder.NONE else SortOrder.PRICE_HIGH_TO_LOW)
            },
            label = { Text("Max") }
        )
        FilterChip(
            selected = currentOrder == SortOrder.DISTANCE,
            onClick = {
                onOrderChange(if (currentOrder == SortOrder.DISTANCE) SortOrder.NONE else SortOrder.DISTANCE)
            },
            label = { Text("Distanza") }
        )
    }
}

@Composable
private fun SearchRadiusSlider(
    radiusKm: Int,
    onRadiusChange: (Int) -> Unit,
    onRadiusChangeFinished: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Raggio di ricerca",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "$radiusKm km",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = radiusKm.toFloat(),
            onValueChange = { onRadiusChange(it.toInt()) },
            onValueChangeFinished = onRadiusChangeFinished,
            valueRange = RADIUS_MIN_KM.toFloat()..RADIUS_MAX_KM.toFloat(),
            steps = RADIUS_MAX_KM - RADIUS_MIN_KM - 1
        )
    }
}

private const val RADIUS_MIN_KM = 1
private const val RADIUS_MAX_KM = 25

@Composable
private fun SuggestionsList(
    suggestions: List<PlaceSuggestion>,
    onSelect: (PlaceSuggestion) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp),
        tonalElevation = 3.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        LazyColumn {
            items(suggestions) { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(suggestion) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = suggestion.label, style = MaterialTheme.typography.bodyMedium)
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun CenterMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                color = tint,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ResultsList(
    result: FuelResult,
    listState: LazyListState,
    favoriteIds: Set<Long>,
    onToggleFavorite: (Station) -> Unit
) {
    if (result.stations.isEmpty()) {
        Column {
            Text(
                text = "Vicino a ${result.location.label}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            AveragePriceRow(result)

            Spacer(modifier = Modifier.height(12.dp))

            CenterMessage(
                icon = Icons.Default.LocalGasStation,
                text = "Nessun distributore trovato nelle vicinanze."
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Text(
                    text = "Vicino a ${result.location.label}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                AveragePriceRow(result)
            }

            item {
                Spacer(modifier = Modifier.height(2.dp))
            }

            items(result.stations) { station ->
                StationCard(
                    station = station,
                    isFavorite = station.id in favoriteIds,
                    onToggleFavorite = { onToggleFavorite(station) }
                )
            }
        }
    }
}

@Composable
private fun AveragePriceRow(result: FuelResult) {
    Row(modifier = Modifier.fillMaxWidth()) {
        AveragePriceTile(
            label = "Benzina media",
            price = result.avgBenzina,
            container = MaterialTheme.colorScheme.secondaryContainer,
            onContainer = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        AveragePriceTile(
            label = "Diesel media",
            price = result.avgDiesel,
            container = MaterialTheme.colorScheme.tertiaryContainer,
            onContainer = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AveragePriceTile(
    label: String,
    price: Double?,
    container: Color,
    onContainer: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = container,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = onContainer)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatPrice(price),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = onContainer
            )
        }
    }
}

@Composable
private fun StationCard(
    station: Station,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = station.name ?: station.brand ?: "Distributore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    val subtitle = listOfNotNull(station.brand, station.address)
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                station.distanceKm?.let {
                    Text(
                        text = "${String.format(Locale.ITALY, "%.1f", it)} km",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (isFavorite) {
                            "Rimuovi dai preferiti"
                        } else "Aggiungi ai preferiti",
                        tint = if (isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PricePill(
                    label = "Benzina",
                    price = station.benzinaPrice,
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    onContainer = MaterialTheme.colorScheme.onSecondaryContainer
                )
                PricePill(
                    label = "Diesel",
                    price = station.dieselPrice,
                    container = MaterialTheme.colorScheme.tertiaryContainer,
                    onContainer = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            station.location?.let { location ->
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            MapNavigation.geoUri(location.lat, location.lng, station.name)
                        )
                        runCatching { ContextCompat.startActivity(context, intent, null) }
                            .onFailure {
                                Toast.makeText(
                                    context,
                                    "Nessuna app di mappe installata.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Portami qui")
                }
            }
        }
    }
}

