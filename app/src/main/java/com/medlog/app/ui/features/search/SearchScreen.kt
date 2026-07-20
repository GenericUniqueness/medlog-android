package com.medlog.app.ui.features.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.medlog.app.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory(
        navController.context.applicationContext as com.medlog.app.MedLogApp
    ))
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        placeholder = { Text("Search health data…") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { /* search is already performed on query change */ }
                        ),
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = MaterialTheme.shapes.extraLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isSearching -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.query.isBlank() -> {
                    EmptySearchState()
                }
                uiState.results.isEmpty -> {
                    NoResultsState(query = uiState.query)
                }
                else -> {
                    SearchResultsList(
                        results = uiState.results,
                        onMedicationClick = { id -> navController.navigate(Route.MedicationDetail.createRoute(id)) },
                        onConditionClick = { id -> navController.navigate(Route.ConditionDetail.createRoute(id)) },
                        onAppointmentClick = { id -> navController.navigate(Route.AppointmentDetail.createRoute(id)) },
                        onJournalClick = { id -> navController.navigate(Route.JournalDetail.createRoute(id)) },
                        onClutterClick = { id -> navController.navigate(Route.Clutter.route) }
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
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Dismiss")
                }
            }
        ) {
            Text(error)
        }
    }
}

@Composable
private fun EmptySearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Type to search across all your health data",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoResultsState(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No results found for '$query'.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Try a different search term.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SearchResultsList(
    results: com.medlog.app.data.local.dao.SearchResults,
    onMedicationClick: (Long) -> Unit,
    onConditionClick: (Long) -> Unit,
    onAppointmentClick: (Long) -> Unit,
    onJournalClick: (Long) -> Unit,
    onClutterClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Medications
        if (results.medications.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Filled.Medication,
                    title = "Medications",
                    count = results.medications.size
                )
            }
            items(results.medications, key = { "med_${it.id}" }) { medication ->
                SearchResultItem(
                    title = medication.name,
                    subtitle = buildString {
                        if (!medication.dosage.isNullOrBlank()) append(medication.dosage)
                        if (!medication.frequency.isNullOrBlank()) {
                            if (isNotEmpty()) append(" · ")
                            append(medication.frequency)
                        }
                    },
                    onClick = { onMedicationClick(medication.id) }
                )
            }
        }

        // Conditions
        if (results.conditions.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Filled.Favorite,
                    title = "Conditions",
                    count = results.conditions.size
                )
            }
            items(results.conditions, key = { "cond_${it.id}" }) { condition ->
                SearchResultItem(
                    title = condition.name,
                    subtitle = buildString {
                        if (!condition.severity.isNullOrBlank()) append(condition.severity.replaceFirstChar { it.uppercase() })
                        append(" · ${condition.status.replaceFirstChar { it.uppercase() }}")
                    },
                    onClick = { onConditionClick(condition.id) }
                )
            }
        }

        // Appointments
        if (results.appointments.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Filled.CalendarToday,
                    title = "Appointments",
                    count = results.appointments.size
                )
            }
            items(results.appointments, key = { "appt_${it.id}" }) { appointment ->
                SearchResultItem(
                    title = appointment.title,
                    subtitle = buildString {
                        append(appointment.appointmentDate.format(
                            java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")
                        ))
                        if (!appointment.doctorName.isNullOrBlank()) {
                            append(" · Dr. ${appointment.doctorName}")
                        }
                    },
                    onClick = { onAppointmentClick(appointment.id) }
                )
            }
        }

        // Journal Entries
        if (results.journalEntries.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Filled.Book,
                    title = "Journal Entries",
                    count = results.journalEntries.size
                )
            }
            items(results.journalEntries, key = { "journal_${it.id}" }) { journal ->
                SearchResultItem(
                    title = journal.title ?: "Journal entry",
                    subtitle = journal.content.take(80),
                    onClick = { onJournalClick(journal.id) }
                )
            }
        }

        // Clutter Items
        if (results.clutterItems.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Filled.Note,
                    title = "Clutter Items",
                    count = results.clutterItems.size
                )
            }
            items(results.clutterItems, key = { "clutter_${it.id}" }) { clutter ->
                SearchResultItem(
                    title = clutter.content.take(60),
                    subtitle = clutter.createdAt.format(
                        java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")
                    ),
                    onClick = { onClutterClick(clutter.id) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SearchResultItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
