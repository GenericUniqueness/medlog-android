package com.medlog.app.ui.features.condition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.entity.ConditionEntity
import com.medlog.app.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConditionListScreen(navController: NavHostController) {
    val viewModel: ConditionViewModel = viewModel(factory = ConditionViewModel.ConditionViewModelFactory(
        navController.context.applicationContext as android.app.Application
    ))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conditions") },
                actions = {
                    IconButton(onClick = { navController.navigate(Route.ProfileSelect.route) }) {
                        Icon(
                            imageVector = Icons.Default.Healing,
                            contentDescription = "Switch Profile"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.activeProfile != null) {
                androidx.compose.material3.FloatingActionButton(
                    onClick = { navController.navigate(Route.AddCondition.route) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Condition")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConditionStatusFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = uiState.statusFilter == filter,
                        onClick = { viewModel.setStatusFilter(filter) },
                        label = { Text(filter.label) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.conditions.isEmpty() && uiState.activeProfile != null -> {
                    EmptyConditionState(filter = uiState.statusFilter)
                }

                uiState.activeProfile == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Healing,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No active profile",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Please select or create a profile first.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 80.dp
                        )
                    ) {
                        items(
                            items = uiState.conditions,
                            key = { it.id }
                        ) { condition ->
                            ConditionCard(
                                condition = condition,
                                onClick = { navController.navigate(Route.ConditionDetail.createRoute(condition.id)) }
                            )
                        }
                    }
                }
            }

            // Error snackbar could go here
            uiState.error?.let { error ->
                SnackbarError(error = error, onDismiss = { viewModel.clearError() })
            }
        }
    }
}

@Composable
private fun ConditionCard(
    condition: ConditionEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = condition.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Severity badge
                if (condition.severity != null) {
                    Badge(
                        containerColor = severityColor(condition.severity),
                        contentColor = Color.White
                    ) {
                        Text(
                            text = condition.severity.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                // Status badge
                Badge(
                    containerColor = statusColor(condition.status),
                    contentColor = Color.White
                ) {
                    Text(
                        text = condition.status.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (condition.diagnosedDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Diagnosed: ${formatDate(condition.diagnosedDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!condition.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = condition.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyConditionState(filter: ConditionStatusFilter) {
    val message = when (filter) {
        ConditionStatusFilter.ALL -> "No conditions tracked yet.\nTap + to add your first condition."
        ConditionStatusFilter.ACTIVE -> "No active conditions.\nAll your conditions are managed or resolved."
        ConditionStatusFilter.MANAGED -> "No managed conditions.\nConditions you're managing will appear here."
        ConditionStatusFilter.RESOLVED -> "No resolved conditions.\nConditions marked as resolved will appear here."
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Healing,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SnackbarError(error: String, onDismiss: () -> Unit) {
    androidx.compose.material3.Snackbar(
        modifier = Modifier.padding(16.dp),
        action = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    ) {
        Text(error)
    }
}

@Composable
private fun TextButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        content()
    }
}

private fun severityColor(severity: String): Color {
    return when (severity.lowercase()) {
        "mild" -> Color(0xFF4CAF50)       // Green
        "moderate" -> Color(0xFFFFA000)    // Amber
        "severe" -> Color(0xFFE53935)      // Red
        else -> Color.Gray
    }
}

private fun statusColor(status: String): Color {
    return when (status.lowercase()) {
        "active" -> Color(0xFFE53935)      // Red
        "managed" -> Color(0xFFFFA000)     // Amber
        "resolved" -> Color(0xFF4CAF50)    // Green
        else -> Color.Gray
    }
}

private fun formatDate(date: java.time.LocalDate): String {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")
    return date.format(formatter)
}
