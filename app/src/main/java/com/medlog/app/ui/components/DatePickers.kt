package com.medlog.app.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

@Composable
fun DatePickerField(
    label: String,
    value: LocalDate?,
    onValueChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    clearable: Boolean = false
) {
    val context = LocalContext.current
    val today = value ?: LocalDate.now()

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value?.format(dateFormatter) ?: "",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            onValueChange(LocalDate.of(year, month + 1, day))
                        },
                        today.year,
                        today.monthValue - 1,
                        today.dayOfMonth
                    ).show()
                },
            trailingIcon = {
                if (clearable && value != null) {
                    TextButton(onClick = { onValueChange(null) }) {
                        Text("Clear", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        )
    }
}

@Composable
fun TimePickerField(
    label: String,
    value: LocalTime?,
    onValueChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val now = value ?: LocalTime.now()

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value?.format(timeFormatter) ?: "",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            onValueChange(LocalTime.of(hour, minute))
                        },
                        now.hour,
                        now.minute,
                        false
                    ).show()
                }
        )
    }
}
