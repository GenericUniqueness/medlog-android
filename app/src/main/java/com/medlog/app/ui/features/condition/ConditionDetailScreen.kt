package com.medlog.app.ui.features.condition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.medlog.app.data.local.entity.ConditionEntity
import com.medlog.app.data.local.entity.ConditionNoteEntity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.medlog.app.MedLogApp
import com.medlog.app.ui.navigation.Route
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionDetailScreen(navController: NavHostController) {
    val viewModel: ConditionViewModel = viewModel(factory = ConditionViewModel.ConditionViewModelFactory(
        navController.context.applicationContext as android.app.Application
    ))
    val uiState by viewModel.uiState.collectAsState()
    val conditionId = navController.currentBackStackEntry
        ?.arguments
        ?.getLong("id") ?: return

    // Load condition detail on first composition
    LaunchedEffect(conditionId) {
        viewModel.selectCondition(conditionId)
    }

    val condition = uiState.selectedCondition
    val notes = uiState.selectedConditionNotes

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showEditNoteDialog by remember { mutableStateOf<ConditionNoteEntity?>(null) }
    var newNoteContent by remember { mutableStateOf("") }
    var editNoteContent by remember { mutableStateOf("") }
    var noteContentError by remember { mutableStateOf<String?>(null) }

    // Delete condition dialog
    val conditionToDelete = uiState.conditionToDelete
    if (uiState.showDeleteDialog && conditionToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Delete Condition") },
            text = {
                Text("Are you sure you want to delete \"${conditionToDelete.name}\"? This will also delete all associated notes. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteCondition(conditionToDelete) },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete note dialog
    if (uiState.showNoteDeleteDialog && uiState.noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissNoteDeleteDialog() },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { uiState.noteToDelete?.let { viewModel.deleteNote(it) } },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissNoteDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add note dialog
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddNoteDialog = false
                newNoteContent = ""
                noteContentError = null
            },
            title = { Text("Add Note") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newNoteContent,
                        onValueChange = {
                            newNoteContent = it
                            noteContentError = if (it.isBlank()) "Note content is required" else null
                        },
                        label = { Text("Note Content *") },
                        isError = noteContentError != null,
                        supportingText = noteContentError?.let { err ->
                            { Text(err, color = MaterialTheme.colorScheme.error) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newNoteContent.isNotBlank()) {
                            viewModel.addNote(conditionId, newNoteContent.trim())
                            newNoteContent = ""
                            noteContentError = null
                            showAddNoteDialog = false
                        } else {
                            noteContentError = "Note content is required"
                        }
                    },
                    enabled = newNoteContent.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddNoteDialog = false
                        newNoteContent = ""
                        noteContentError = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit note dialog
    if (showEditNoteDialog != null) {
        AlertDialog(
            onDismissRequest = {
                showEditNoteDialog = null
                editNoteContent = ""
                noteContentError = null
            },
            title = { Text("Edit Note") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editNoteContent,
                        onValueChange = {
                            editNoteContent = it
                            noteContentError = if (it.isBlank()) "Note content is required" else null
                        },
                        label = { Text("Note Content *") },
                        isError = noteContentError != null,
                        supportingText = noteContentError?.let { err ->
                            { Text(err, color = MaterialTheme.colorScheme.error) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editNoteContent.isNotBlank() && showEditNoteDialog != null) {
                            viewModel.updateNote(
                                showEditNoteDialog!!.copy(content = editNoteContent.trim())
                            )
                            showEditNoteDialog = null
                            editNoteContent = ""
                            noteContentError = null
                        } else {
                            noteContentError = "Note content is required"
                        }
                    },
                    enabled = editNoteContent.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditNoteDialog = null
                        editNoteContent = ""
                        noteContentError = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = condition?.name ?: "Condition",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (condition != null) {
                        IconButton(onClick = { navController.navigate(Route.AddCondition.route) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { viewModel.showDeleteConditionDialog(condition) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (condition != null) {
                FloatingActionButton(
                    onClick = { showAddNoteDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note")
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            condition != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 88.dp)
                ) {
                    // Overview card
                    item {
                        ConditionOverviewCard(condition = condition)
                    }

                    // Notes section header
                    item {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Notes list
                    if (notes.isEmpty()) {
                        item {
                            EmptyNotesState()
                        }
                    } else {
                        items(
                            items = notes,
                            key = { it.id }
                        ) { note ->
                            NoteCard(
                                note = note,
                                onEdit = {
                                    editNoteContent = note.content
                                    showEditNoteDialog = note
                                },
                                onDelete = { viewModel.showDeleteNoteDialog(note) }
                            )
                        }
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Condition not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ConditionOverviewCard(condition: ConditionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = condition.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Severity
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Severity: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Badge(
                    containerColor = severityColor(condition.severity),
                    contentColor = Color.White
                ) {
                    Text(
                        text = (condition.severity ?: "Not set").replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Status: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Badge(
                    containerColor = conditionStatusColor(condition.status),
                    contentColor = Color.White
                ) {
                    Text(
                        text = condition.status.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Diagnosed date
            condition.diagnosedDate?.let { date ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Diagnosed: ${date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Notes
            if (!condition.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = condition.notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: ConditionNoteEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.noteDate.format(
                        DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Note",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EmptyNotesState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Note,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No notes yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap + to add a note about this condition.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun severityColor(severity: String?): Color {
    return when (severity?.lowercase()) {
        "mild" -> Color(0xFF4CAF50)
        "moderate" -> Color(0xFFFFA000)
        "severe" -> Color(0xFFE53935)
        else -> Color.Gray
    }
}

private fun conditionStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "active" -> Color(0xFFE53935)
        "managed" -> Color(0xFFFFA000)
        "resolved" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}
