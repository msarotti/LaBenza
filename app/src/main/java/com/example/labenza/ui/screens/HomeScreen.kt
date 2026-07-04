package com.example.labenza.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.labenza.data.model.Country
import com.example.labenza.ui.viewmodel.AverageUiState
import com.example.labenza.ui.viewmodel.FuelViewModel
import java.util.Locale

/**
 * Landing screen: pick a country (only Italy for now), see its national average
 * benzina/diesel price (fetched from MIMIT's regional CSV), and jump to the
 * station-search screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FuelViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToRegional: () -> Unit
) {
    var selectedCountry by remember { mutableStateOf(Country.default) }
    val averageState by viewModel.averages.collectAsState()

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
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Paese",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            CountrySelector(
                selected = selectedCountry,
                onSelect = { selectedCountry = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Prezzo medio nazionale",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val loading = averageState is AverageUiState.Loading
            val data = (averageState as? AverageUiState.Success)?.data

            Row(modifier = Modifier.fillMaxWidth()) {
                NationalAverageTile(
                    label = "Benzina",
                    price = data?.nationalBenzina,
                    loading = loading,
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    onContainer = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                NationalAverageTile(
                    label = "Diesel",
                    price = data?.nationalDiesel,
                    loading = loading,
                    container = MaterialTheme.colorScheme.tertiaryContainer,
                    onContainer = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (val state = averageState) {
                is AverageUiState.Loading -> Text(
                    text = "Caricamento dei prezzi medi…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                is AverageUiState.Success -> {
                    val caption = state.data.updated
                        ?.let { "Media nazionale · aggiornato il $it" }
                        ?: "Media nazionale indicativa."
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is AverageUiState.Error -> Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Impossibile caricare i prezzi medi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = { viewModel.loadAverages() }) {
                        Text("Riprova")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToRegional,
                enabled = data != null,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Prezzo per regione", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNavigateToSearch,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerca distributori", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountrySelector(
    selected: Country,
    onSelect: (Country) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = "${selected.flag}  ${selected.displayName}",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Country.selectable.forEach { country ->
                DropdownMenuItem(
                    text = { Text("${country.flag}  ${country.displayName}") },
                    onClick = {
                        onSelect(country)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun NationalAverageTile(
    label: String,
    price: Double?,
    loading: Boolean,
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
            if (loading) {
                CircularProgressIndicator(
                    color = onContainer,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = if (price != null) {
                        "${String.format(Locale.ITALY, "%.3f", price)} €/l"
                    } else "N/A",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = onContainer
                )
            }
        }
    }
}
