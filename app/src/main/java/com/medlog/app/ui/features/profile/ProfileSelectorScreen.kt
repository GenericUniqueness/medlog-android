package com.medlog.app.ui.features.profile

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.ui.components.ProfileAvatar
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSelectorScreen(
    onProfileSelected: () -> Unit
) {
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(
        LocalContext.current.applicationContext as MedLogApp
    ))
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Profile") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Profile")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.profiles.isEmpty() -> {
                    EmptyProfilesState(
                        onCreateClick = { showCreateDialog = true }
                    )
                }
                else -> {
                    ProfileList(
                        profiles = uiState.profiles,
                        activeProfile = uiState.activeProfile,
                        onProfileClick = { profile ->
                            viewModel.switchProfile(profile.id)
                            onProfileSelected()
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProfileDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, dob, bloodType, allergies, notes ->
                viewModel.createProfile(name, dob, bloodType, allergies, notes)
                showCreateDialog = false
            }
        )
    }

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
private fun EmptyProfilesState(
    onCreateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "No profiles yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create your first profile to get started.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledTonalButton(onClick = onCreateClick) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Profile")
        }
    }
}

@Composable
private fun ProfileList(
    profiles: List<ProfileEntity>,
    activeProfile: ProfileEntity?,
    onProfileClick: (ProfileEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(profiles, key = { it.id }) { profile ->
            ProfileCard(
                profile = profile,
                isActive = profile.id == activeProfile?.id,
                onClick = { onProfileClick(profile) }
            )
        }
    }
}

@Composable
private fun ProfileCard(
    profile: ProfileEntity,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(
                name = profile.name,
                size = 48.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                profile.dateOfBirth?.let { dob ->
                    Text(
                        text = "Born ${dob.format(dateFormatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isActive) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Active",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateProfileDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, dob: LocalDate?, bloodType: String?, allergies: String?, notes: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var dateOfBirth by remember { mutableStateOf<LocalDate?>(null) }
    var bloodType by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val bloodTypeOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    var bloodTypeExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Profile") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = if (it.isBlank()) "Name is required" else null
                    },
                    label = { Text("Name") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateOfBirth?.format(dateFormatter) ?: "",
                    onValueChange = {},
                    label = { Text("Date of Birth (optional)") },
                    readOnly = true,
                    trailingIcon = {
                        TextButton(onClick = {
                            val today = LocalDate.now()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    dateOfBirth = LocalDate.of(year, month + 1, day)
                                },
                                today.year,
                                today.monthValue - 1,
                                today.dayOfMonth
                            ).show()
                        }) {
                            Text("Pick")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = bloodTypeExpanded,
                    onExpandedChange = { bloodTypeExpanded = !bloodTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = bloodType,
                        onValueChange = {},
                        label = { Text("Blood Type (optional)") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = bloodTypeExpanded,
                        onDismissRequest = { bloodTypeExpanded = false }
                    ) {
                        bloodTypeOptions.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    bloodType = type
                                    bloodTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Allergies (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Name is required"
                        return@TextButton
                    }
                    onCreate(
                        name.trim(),
                        dateOfBirth,
                        bloodType.ifBlank { null },
                        allergies.ifBlank { null },
                        notes.ifBlank { null }
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
