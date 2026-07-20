package com.medlog.app.ui.features.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.ui.navigation.Route
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(
        navController.context.applicationContext as com.medlog.app.MedLogApp
    ))
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf<ProfileEntity?>(null) }
    var showDeleteProfileDialog by remember { mutableStateOf<ProfileEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Profile Section
            SectionTitle("Profile")

            // Active profile display
            val activeProfile = uiState.activeProfile
            if (activeProfile != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = activeProfile.name.firstOrNull()?.uppercase() ?: "?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeProfile.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Active Profile",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Profile list
            uiState.profiles.forEach { profile ->
                ProfileListItem(
                    profile = profile,
                    isActive = profile.id == uiState.activeProfile?.id,
                    onSwitch = { viewModel.switchProfile(profile.id) },
                    onEdit = { showEditProfileDialog = profile },
                    onDelete = {
                        if (uiState.profiles.size > 1) {
                            showDeleteProfileDialog = profile
                        } else {
                            Toast.makeText(context, "Cannot delete the only profile", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Add Profile button
            OutlinedButton(
                onClick = { showAddProfileDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Profile")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notifications Section
            SectionTitle("Notifications")

            SwitchSettingItem(
                title = "Medication Reminders",
                subtitle = "Get reminders for medication intake",
                checked = uiState.medicationRemindersEnabled,
                onCheckedChange = { viewModel.setMedicationRemindersEnabled(it) }
            )

            SwitchSettingItem(
                title = "Appointment Reminders",
                subtitle = "Get reminders for upcoming appointments",
                checked = uiState.appointmentRemindersEnabled,
                onCheckedChange = { viewModel.setAppointmentRemindersEnabled(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Data Section
            SectionTitle("Data")

            Button(
                onClick = { viewModel.exportDataAsPdf() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isExporting && uiState.activeProfile != null
            ) {
                if (uiState.isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exporting…")
                } else {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export as PDF")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            SectionTitle("About")

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MedLog",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MedLog — Your Private Health Journal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add Profile Dialog
    if (showAddProfileDialog) {
        AddEditProfileDialog(
            existingProfile = null,
            onConfirm = { name, dob, bloodType, allergies ->
                viewModel.createProfile(name, dob, bloodType, allergies)
                showAddProfileDialog = false
            },
            onDismiss = { showAddProfileDialog = false }
        )
    }

    // Edit Profile Dialog
    showEditProfileDialog?.let { profile ->
        AddEditProfileDialog(
            existingProfile = profile,
            onConfirm = { name, dob, bloodType, allergies ->
                viewModel.updateProfile(
                    profile.copy(
                        name = name,
                        dateOfBirth = dob,
                        bloodType = bloodType,
                        allergies = allergies
                    )
                )
                showEditProfileDialog = null
            },
            onDismiss = { showEditProfileDialog = null }
        )
    }

    // Delete Profile Confirmation
    showDeleteProfileDialog?.let { profile ->
        AlertDialog(
            onDismissRequest = { showDeleteProfileDialog = null },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Profile?") },
            text = {
                Text("Are you sure you want to delete \"${profile.name}\"? All data associated with this profile, including medications, conditions, appointments, and journal entries, will be permanently lost. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProfile(profile)
                        showDeleteProfileDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteProfileDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export success message
    LaunchedEffect(uiState.exportSuccess) {
        if (uiState.exportSuccess) {
            Toast.makeText(context, "Report exported successfully", Toast.LENGTH_SHORT).show()
            viewModel.clearExportSuccess()
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
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ProfileListItem(
    profile: ProfileEntity,
    isActive: Boolean,
    onSwitch: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = if (!isActive) onSwitch else {{}})
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = profile.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isActive) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (!isActive) {
                FilledTonalButton(
                    onClick = onSwitch,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Switch", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditProfileDialog(
    existingProfile: ProfileEntity?,
    onConfirm: (name: String, dob: LocalDate?, bloodType: String?, allergies: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(existingProfile?.name ?: "") }
    var bloodType by remember { mutableStateOf(existingProfile?.bloodType ?: "") }
    var allergies by remember { mutableStateOf(existingProfile?.allergies ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }

    val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingProfile != null) "Edit Profile" else "Create Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                // Blood type dropdown
                var bloodTypeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = bloodTypeExpanded,
                    onExpandedChange = { bloodTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = bloodType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Blood Type (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = bloodTypeExpanded,
                        onDismissRequest = { bloodTypeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Not specified") },
                            onClick = {
                                bloodType = ""
                                bloodTypeExpanded = false
                            }
                        )
                        bloodTypes.forEach { type ->
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
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
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
                    onConfirm(
                        name.trim(),
                        existingProfile?.dateOfBirth,
                        bloodType.ifBlank { null },
                        allergies.ifBlank { null }
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (existingProfile != null) "Save" else "Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
