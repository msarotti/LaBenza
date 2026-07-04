package com.example.labenza.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

/** Small labelled price chip shared by the search and favorites cards. */
@Composable
fun PricePill(
    label: String,
    price: Double?,
    container: Color,
    onContainer: Color
) {
    Surface(color = container, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = onContainer)
            Text(
                text = formatPrice(price),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = onContainer
            )
        }
    }
}

fun formatPrice(price: Double?): String =
    if (price != null) "${String.format(Locale.ITALY, "%.3f", price)} €/l" else "N/A"
