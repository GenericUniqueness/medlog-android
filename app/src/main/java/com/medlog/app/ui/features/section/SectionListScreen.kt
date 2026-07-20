package com.medlog.app.ui.features.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.medlog.app.ui.features.profile.ProfileSwitcher
import com.medlog.app.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionListScreen(navController: NavHostController) {
    val viewModel: SectionViewModel = viewModel(factory = SectionViewModel.Factory(
        navController.context.applicationContext as android.app.Application
    ))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sections") },
                actions = {
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
                    onClick = { navController.navigate(Route.AddSection.route) }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Section")
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.activeProfile == null -> {
                NoActiveProfileState(
                    onManageProfiles = { navController.navigate(Route.ProfileSelect.route) }
                )
            }
            uiState.sectionsWithEntries.isEmpty() -> {
                EmptySectionState()
            }
            else -> {
                SectionLazyColumn(
                    sectionsWithEntries = uiState.sectionsWithEntries,
                    expandedSectionIds = uiState.expandedSectionIds,
                    onToggleExpand = { viewModel.toggleSectionExpanded(it) },
                    onSectionClick = { sectionId ->
                        navController.navigate(Route.SectionDetail.createRoute(sectionId))
                    },
                    onAddEntry = { sectionId ->
                        // Navigate to section detail and trigger add entry
                        navController.navigate(Route.SectionDetail.createRoute(sectionId))
                    },
                    modifier = Modifier.padding(innerPadding)
                )
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
            imageVector = Icons.Filled.Folder,
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
            text = "Select or create a profile to view sections.",
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
private fun EmptySectionState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Folder,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No custom sections",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create one to organize your data.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SectionLazyColumn(
    sectionsWithEntries: List<SectionWithEntries>,
    expandedSectionIds: Set<Long>,
    onToggleExpand: (Long) -> Unit,
    onSectionClick: (Long) -> Unit,
    onAddEntry: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sectionsWithEntries, key = { it.section.id }) { sectionWithEntries ->
            ExpandableSectionCard(
                sectionWithEntries = sectionWithEntries,
                isExpanded = sectionWithEntries.section.id in expandedSectionIds,
                onToggleExpand = { onToggleExpand(sectionWithEntries.section.id) },
                onSectionClick = { onSectionClick(sectionWithEntries.section.id) },
                onAddEntry = { onAddEntry(sectionWithEntries.section.id) }
            )
        }
    }
}

@Composable
private fun ExpandableSectionCard(
    sectionWithEntries: SectionWithEntries,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSectionClick: () -> Unit,
    onAddEntry: () -> Unit
) {
    val section = sectionWithEntries.section
    val entries = sectionWithEntries.entries

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Section header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSectionClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${entries.size} ${if (entries.size == 1) "entry" else "entries"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Expand/collapse button
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            // Expanded content: entries
            if (isExpanded && entries.isNotEmpty()) {
                HorizontalDivider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entries.forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (!entry.content.isNullOrBlank()) {
                                Text(
                                    text = entry.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Add entry button within expanded section
                TextButton(
                    onClick = onAddEntry,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Entry")
                }
            }
        }
    }
}
