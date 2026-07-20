package com.medlog.app.ui.features.medication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.entity.MedicationEntity
import com.medlog.app.ui.features.profile.ProfileSwitcher
import com.medlog.app.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(navController: NavHostController) {
    val viewModel: MedicationViewModel = viewModel(factory = MedicationViewModel.Factory(
        navController.context.applicationContext as MedLogApp
    ))
    val uiState by viewModel.uiState.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.setSearchQuery(it)
                            },
                            placeholder = { Text("Search medications…") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Text("Medications")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) {
                            searchQuery = ""
                            viewModel.setSearchQuery("")
                        }
                    }) {
                        Icon(
                            imageVector = if (showSearch) Icons.Filled.Check else Icons.Filled.Search,
                            contentDescription = if (showSearch) "Done" else "Search"
                        )
                    }
                    ProfileSwitcher(
                        activeProfile = uiState.activeProfile,
                        allProfiles = uiState.allProfiles,
                        onSwitchProfile = { viewModel.switchProfile(it) },
                        onManageProfiles = { navController.navigate(Route.ProfileSelect.route) }
                    )
                }
            )
        },
        floatingActionButton = {
            if (uiState.activeProfile != null) {
                FloatingActionButton(
                    onClick = { navController.navigate(Route.AddMedication.route) }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Medication")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter chips
            MedicationFilterChips(
                selectedFilter = uiState.statusFilter,
                onFilterSelected = { viewModel.setStatusFilter(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.activeProfile == null -> {
                    NoActiveProfileState(onManageProfiles = { navController.navigate(Route.ProfileSelect.route) })
                }
                uiState.filteredMedications.isEmpty() -> {
                    EmptyMedicationState(filter = uiState.statusFilter)
                }
                else -> {
                    MedicationLazyColumn(
                        medications = uiState.filteredMedications,
                        onMedicationClick = { navController.navigate(Route.MedicationDetail.createRoute(it.id)) },
                        onLogClick = { medication ->
                            viewModel.logMedicationIntake(
                                medicationId = medication.id,
                                dosageTaken = medication.dosage,
                                notes = null
                            )
                        }
                    )
                }
            }
        }
    }

    // Error snackbar
    uiState.error?.let { error ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { viewModel.clearListError() }) {
                    Text("Dismiss")
                }
            }
        ) {
            Text(error)
        }
    }
}

@Composable
private fun MedicationFilterChips(
    selectedFilter: MedicationStatusFilter,
    onFilterSelected: (MedicationStatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MedicationStatusFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) }
            )
        }
    }
}

@Composable
private fun NoActiveProfileState(
    onManageProfiles: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Medication,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No active profile",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select or create a profile to view medications.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledTonalButton(onClick = onManageProfiles) {
            Text("Manage Profiles")
        }
    }
}

@Composable
private fun EmptyMedicationState(
    filter: MedicationStatusFilter
) {
    val (title, description) = when (filter) {
        MedicationStatusFilter.ALL -> "No medications" to "Add your first medication to start tracking."
        MedicationStatusFilter.ACTIVE -> "No active medications" to "When you add medications and set them to active, they'll appear here."
        MedicationStatusFilter.PAUSED -> "No paused medications" to "Medications you pause will appear here."
        MedicationStatusFilter.DISCONTINUED -> "No discontinued medications" to "Medications you discontinue will appear here."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Medication,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun MedicationLazyColumn(
    medications: List<MedicationEntity>,
    onMedicationClick: (MedicationEntity) -> Unit,
    onLogClick: (MedicationEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(medications, key = { it.id }) { medication ->
            MedicationCard(
                medication = medication,
                onClick = { onMedicationClick(medication) },
                onLogClick = { onLogClick(medication) }
            )
        }
    }
}

@Composable
private fun MedicationCard(
    medication: MedicationEntity,
    onClick: () -> Unit,
    onLogClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Dosage and frequency on second line
                val dosageFrequency = buildString {
                    if (!medication.dosage.isNullOrBlank()) {
                        append(medication.dosage)
                    }
                    if (!medication.frequency.isNullOrBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append(medication.frequency)
                    }
                }
                if (dosageFrequency.isNotEmpty()) {
                    Text(
                        text = dosageFrequency,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Status badge
                StatusBadge(status = medication.status)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quick "Log" button
            if (medication.status == "active") {
                FilledTonalButton(
                    onClick = onLogClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Log", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (label, containerColor, contentColor) = when (status) {
        "active" -> Triple(
            "Active",
            Color(0xFFDCFCE7),  // green-100
            Color(0xFF166534)   // green-800
        )
        "paused" -> Triple(
            "Paused",
            Color(0xFFFEF3C7),  // amber-100
            Color(0xFF92400E)   // amber-800
        )
        "discontinued" -> Triple(
            "Discontinued",
            Color(0xFFF3F4F6),  // gray-100
            Color(0xFF4B5563)   // gray-600
        )
        else -> Triple(
            status.replaceFirstChar { it.uppercase() },
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}


