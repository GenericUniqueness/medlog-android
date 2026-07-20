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
fun SeverityBadge(severity: String?) {
    if (severity == null) return
    val (color, containerColor) = when (severity.lowercase()) {
        "mild" -> Color(0xFF2E7D32) to Color(0xFFC8E6C9)
        "moderate" -> Color(0xFFF57F17) to Color(0xFFFFF9C4)
        "severe" -> Color(0xFFC62828) to Color(0xFFFFCDD2)
        else -> Color.Gray to Color.LightGray
    }
    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = severity.replaceFirstChar { it.uppercase() },
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
