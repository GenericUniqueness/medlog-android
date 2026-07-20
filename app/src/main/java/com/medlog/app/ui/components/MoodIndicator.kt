package com.medlog.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MoodIndicator(mood: String?, showLabel: Boolean = false) {
    if (mood == null) return
    val (emoji, label, color) = when (mood.lowercase()) {
        "great" -> Triple("😊", "Great", Color(0xFF2E7D32))
        "good" -> Triple("🙂", "Good", Color(0xFF558B2F))
        "okay" -> Triple("😐", "Okay", Color(0xFFF57F17))
        "bad" -> Triple("😞", "Bad", Color(0xFFE65100))
        "terrible" -> Triple("😢", "Terrible", Color(0xFFC62828))
        else -> Triple("❓", mood, Color.Gray)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, style = MaterialTheme.typography.bodyLarge)
        if (showLabel) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
fun MoodSelector(selectedMood: String?, onMoodSelected: (String) -> Unit) {
    val moods = listOf("great", "good", "okay", "bad", "terrible")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        moods.forEach { mood ->
            val emoji = when (mood) {
                "great" -> "😊"
                "good" -> "🙂"
                "okay" -> "😐"
                "bad" -> "😞"
                "terrible" -> "😢"
                else -> "❓"
            }
            FilterChip(
                selected = selectedMood == mood,
                onClick = { onMoodSelected(mood) },
                label = { Text(emoji, style = MaterialTheme.typography.titleMedium) },
                modifier = Modifier.size(56.dp)
            )
        }
    }
}
