package com.medlog.app.ui.features.medication

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.entity.MedicationEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val frequencyOptions = listOf(
    "Once daily",
    "Twice daily",
    "Three times daily",
    "As needed",
    "Custom"
)

private val statusOptions = listOf(
    "active" to "Active",
    "paused" to "Paused",
    "discontinued" to "Discontinued"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(navController: NavHostController) {
    val viewModel: MedicationViewModel = viewModel(factory = MedicationViewModel.Factory(
        navController.context.applicationContext as MedLogApp
    ))
    val existingMedication: MedicationEntity? = null
    val isEditing = existingMedication != null

    // Form state
    var name by remember { mutableStateOf(existingMedication?.name ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var dosage by remember { mutableStateOf(existingMedication?.dosage ?: "") }
    var frequency by remember { mutableStateOf(existingMedication?.frequency ?: "") }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(existingMedication?.startDate) }
    var endDate by remember { mutableStateOf(existingMedication?.endDate) }
    var selectedStatus by remember { mutableStateOf(existingMedication?.status ?: "active") }
    var statusExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(existingMedication?.notes ?: "") }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Validate name whenever it changes
    LaunchedEffect(name) {
        nameError = if (name.isBlank() && name.isNotEmpty()) "Name is required" else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Medication" else "Add Medication") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name (required)
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = if (it.isBlank()) "Name is required" else null
                },
                label = { Text("Name *") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Dosage (optional)
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage (e.g., 500mg)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Frequency (optional, dropdown)
            ExposedDropdownMenuBox(
                expanded = frequencyExpanded,
                onExpandedChange = { frequencyExpanded = !frequencyExpanded }
            ) {
                OutlinedTextField(
                    value = frequency,
                    onValueChange = {
                        frequency = it
                        frequencyExpanded = false
                    },
                    label = { Text("Frequency") },
                    readOnly = false,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = frequencyExpanded,
                    onDismissRequest = { frequencyExpanded = false }
                ) {
                    frequencyOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                frequency = if (option == "Custom") "" else option
                                frequencyExpanded = false
                            }
                        )
                    }
                }
            }

            // Start date (optional, date picker)
            OutlinedTextField(
                value = startDate?.format(dateFormatter) ?: "",
                onValueChange = {},
                label = { Text("Start Date (optional)") },
                readOnly = true,
                trailingIcon = {
                    Row {
                        if (startDate != null) {
                            TextButton(onClick = { startDate = null }) {
                                Text("Clear")
                            }
                        }
                        TextButton(onClick = {
                            val today = LocalDate.now()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    startDate = LocalDate.of(year, month + 1, day)
                                },
                                startDate?.year ?: today.year,
                                (startDate?.monthValue ?: today.monthValue) - 1,
                                startDate?.dayOfMonth ?: today.dayOfMonth
                            ).show()
                        }) {
                            Text("Pick")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // End date (optional, date picker)
            OutlinedTextField(
                value = endDate?.format(dateFormatter) ?: "",
                onValueChange = {},
                label = { Text("End Date (optional)") },
                readOnly = true,
                trailingIcon = {
                    Row {
                        if (endDate != null) {
                            TextButton(onClick = { endDate = null }) {
                                Text("Clear")
                            }
                        }
                        TextButton(onClick = {
                            val today = LocalDate.now()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    endDate = LocalDate.of(year, month + 1, day)
                                },
                                endDate?.year ?: today.year,
                                (endDate?.monthValue ?: today.monthValue) - 1,
                                endDate?.dayOfMonth ?: today.dayOfMonth
                            ).show()
                        }) {
                            Text("Pick")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Status (dropdown)
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded }
            ) {
                OutlinedTextField(
                    value = statusOptions.first { it.first == selectedStatus }.second,
                    onValueChange = {},
                    label = { Text("Status") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    statusOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedStatus = value
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            // Notes (optional, multiline)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Name is required"
                        return@Button
                    }
                    if (isEditing && existingMedication != null) {
                        viewModel.updateMedication(
                            existingMedication.copy(
                                name = name.trim(),
                                dosage = dosage.trim().ifBlank { null },
                                frequency = frequency.trim().ifBlank { null },
                                startDate = startDate,
                                endDate = endDate,
                                status = selectedStatus,
                                notes = notes.trim().ifBlank { null }
                            )
                        )
                    } else {
                        viewModel.createMedication(
                            name = name.trim(),
                            dosage = dosage.trim().ifBlank { null },
                            frequency = frequency.trim().ifBlank { null },
                            startDate = startDate,
                            endDate = endDate,
                            status = selectedStatus,
                            notes = notes.trim().ifBlank { null }
                        )
                    }
                    navController.popBackStack()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Save Changes" else "Add Medication")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
