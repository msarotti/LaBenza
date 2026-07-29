package com.example.labenza.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.labenza.data.model.Country
import com.example.labenza.data.model.FavoriteStation
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
    val favorites by viewModel.favorites.collectAsState()
    val context = LocalContext.current

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

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Preferiti",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "Nessun distributore preferito. Tocca la stella su un " +
                            "distributore nella ricerca per aggiungerlo qui.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(favorites, key = { it.id }) { favorite ->
                        FavoriteStationCard(
                            favorite = favorite,
                            onRemove = { viewModel.removeFavorite(favorite.id) },
                            onSearchHere = {
                                val lat = favorite.lat
                                val lng = favorite.lng
                                if (lat != null && lng != null) {
                                    val label = favorite.name
                                        ?: favorite.brand
                                        ?: favorite.address
                                        ?: "Distributore preferito"
                                    viewModel.searchNearby(lat, lng, label)
                                    onNavigateToSearch()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            KofiLink(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, KOFI_URL.toUri())
                    ContextCompat.startActivity(context, intent, null)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            DataSourceNotice()

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/** Attribution for the MIMIT open data used by the app, required by the IODL licence. */
@Composable
private fun DataSourceNotice() {
    Text(
        text = "Dati sui prezzi dei carburanti forniti dal MIMIT — Ministero delle " +
            "Imprese e del Made in Italy (Osservaprezzi Carburanti) e distribuiti " +
            "con licenza IODL — Italian Open Data License 2.0.",
        style = MaterialTheme.typography.bodySmall,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

/** Ko-fi "Support me" button, styled in Ko-fi's brand blue (#72A4F2). */
@Composable
private fun KofiLink(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Coffee,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Supportami su Ko-fi",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private const val KOFI_URL = "https://ko-fi.com/Y3C423RFZ6"

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
private fun FavoriteStationCard(
    favorite: FavoriteStation,
    onRemove: () -> Unit,
    onSearchHere: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = favorite.name ?: favorite.brand ?: "Distributore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    val subtitle = listOfNotNull(favorite.brand, favorite.address)
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
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rimuovi dai preferiti",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (favorite.lat != null && favorite.lng != null) {
                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = onSearchHere,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerca da qui")
                }
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
