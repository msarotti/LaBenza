package com.example.labenza.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import java.util.Locale

/**
 * Landing screen: pick a country (only Italy for now), see its national average
 * benzina/diesel price, and jump to the station-search screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToSearch: () -> Unit) {
    var selectedCountry by remember { mutableStateOf(Country.default) }

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

            Row(modifier = Modifier.fillMaxWidth()) {
                NationalAverageTile(
                    label = "Benzina",
                    price = selectedCountry.avgBenzina,
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    onContainer = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                NationalAverageTile(
                    label = "Diesel",
                    price = selectedCountry.avgDiesel,
                    container = MaterialTheme.colorScheme.tertiaryContainer,
                    onContainer = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Media indicativa a livello nazionale.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
