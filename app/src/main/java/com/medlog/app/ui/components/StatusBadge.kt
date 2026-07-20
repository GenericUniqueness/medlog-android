package com.medlog.app.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusBadge(status: String) {
    val (color, containerColor) = when (status.lowercase()) {
        "active", "scheduled" -> Color(0xFF2E7D32) to Color(0xFFC8E6C9)
        "managed", "completed" -> Color(0xFF1565C0) to Color(0xFFBBDEFB)
        "paused" -> Color(0xFFF57F17) to Color(0xFFFFF9C4)
        "resolved" -> Color(0xFF616161) to Color(0xFFE0E0E0)
        "discontinued", "cancelled" -> Color(0xFFC62828) to Color(0xFFFFCDD2)
        else -> Color.Gray to Color.LightGray
    }
    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = status.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor,
            labelColor = color
        ),
        border = null,
        modifier = Modifier.height(24.dp)
    )
}
