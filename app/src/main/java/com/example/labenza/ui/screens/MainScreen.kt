package com.example.labenza.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.labenza.data.model.PlaceSuggestion
import com.example.labenza.data.model.Station
import com.example.labenza.ui.viewmodel.FuelResult
import com.example.labenza.ui.viewmodel.FuelUiState
import com.example.labenza.ui.viewmodel.FuelViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FuelViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("LaBenza - Prezzi Carburante") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Cerca per città o indirizzo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Cancella")
                        }
                    }
                }
            )

            // Autocomplete suggestions (OpenStreetMap / Nominatim).
            if (suggestions.isNotEmpty()) {
                SuggestionsList(
                    suggestions = suggestions,
                    onSelect = viewModel::selectSuggestion
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.fetchByLocation() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Usa la mia posizione")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is FuelUiState.Idle -> CenterMessage("Inserisci un indirizzo o usa la posizione GPS")
                is FuelUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                is FuelUiState.Success -> ResultsList(state.result)
                is FuelUiState.Error -> CenterMessage(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

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
        shape = MaterialTheme.shapes.medium
    ) {
        LazyColumn {
            items(suggestions) { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(suggestion) }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
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
    text: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, color = color)
    }
}

@Composable
private fun ResultsList(result: FuelResult) {
    Column {
        Text(
            text = "Risultati vicino a: ${result.location.label}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        AveragePriceCard(result.avgBenzina, result.avgDiesel)

        Spacer(modifier = Modifier.height(8.dp))

        if (result.stations.isEmpty()) {
            CenterMessage("Nessun distributore trovato nelle vicinanze.")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(result.stations) { station -> DistributorItem(station) }
            }
        }
    }
}

@Composable
fun AveragePriceCard(benzina: Double?, diesel: Double?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Media zona:", fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Benzina: ${formatPrice(benzina)}")
                Text("Diesel: ${formatPrice(diesel)}")
            }
        }
    }
}

@Composable
fun DistributorItem(station: Station) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = station.name ?: station.brand ?: "Distributore",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            val subtitle = listOfNotNull(station.brand, station.address)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
            if (subtitle.isNotBlank()) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PriceText("Benzina", station.benzinaPrice)
                PriceText("Diesel", station.dieselPrice)
            }
            station.distanceKm?.let {
                Text(
                    text = "Distanza: ${String.format(Locale.ITALY, "%.1f", it)} km",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun PriceText(label: String, price: Double?) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(
            text = formatPrice(price),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun formatPrice(price: Double?): String =
    if (price != null) "${String.format(Locale.ITALY, "%.3f", price)} €/l" else "N/A"
