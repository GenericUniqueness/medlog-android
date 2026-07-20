package com.medlog.app.ui.features.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.medlog.app.data.local.entity.MedicationEntity
import com.medlog.app.ui.features.profile.ProfileSwitcher
import com.medlog.app.ui.navigation.Route
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(
        navController.context.applicationContext as com.medlog.app.MedLogApp
    ))
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogMedicationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MedLog") },
                actions = {
                    IconButton(onClick = { navController.navigate(Route.Search.route) }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { navController.navigate(Route.Settings.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                    ProfileSwitcher(
                        activeProfile = uiState.activeProfile,
                        allProfiles = uiState.allProfiles,
                        onSwitchProfile = { viewModel.switchProfile(it) },
                        onManageProfiles = { navController.navigate(Route.Settings.route) }
                    )
                }
            )
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Person,
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
                            text = "Create a profile to get started.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        FilledTonalButton(onClick = { navController.navigate(Route.Settings.route) }) {
                            Text("Create Profile")
                        }
                    }
                }
            }
            else -> {
                DashboardContent(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding),
                    onMedicationsClick = { navController.navigate(Route.Medications.route) },
                    onConditionsClick = { navController.navigate(Route.Conditions.route) },
                    onAppointmentsClick = { navController.navigate(Route.Appointments.route) },
                    onLogMedication = { showLogMedicationDialog = true },
                    onNewJournal = { navController.navigate(Route.AddJournal.route) },
                    onNewNote = { navController.navigate(Route.Clutter.route) },
                    onAppointmentClick = { id -> navController.navigate(Route.AppointmentDetail.createRoute(id)) },
                    onActivityClick = { item ->
                        when (item.type) {
                            "medication_log" -> navController.navigate(Route.MedicationDetail.createRoute(item.entityId))
                            "condition" -> navController.navigate(Route.ConditionDetail.createRoute(item.entityId))
                            "journal" -> navController.navigate(Route.JournalDetail.createRoute(item.entityId))
                            "appointment" -> navController.navigate(Route.AppointmentDetail.createRoute(item.entityId))
                        }
                    }
                )
            }
        }
    }

    // Log Medication Dialog
    if (showLogMedicationDialog) {
        QuickLogMedicationDialog(
            medications = uiState.activeMedications,
            onLog = { medicationId, dosage, notes ->
                viewModel.logMedicationIntake(medicationId, dosage, notes)
                showLogMedicationDialog = false
            },
            onDismiss = { showLogMedicationDialog = false }
        )
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
private fun DashboardContent(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier,
    onMedicationsClick: () -> Unit,
    onConditionsClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onLogMedication: () -> Unit,
    onNewJournal: () -> Unit,
    onNewNote: () -> Unit,
    onAppointmentClick: (Long) -> Unit,
    onActivityClick: (RecentActivityItem) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Greeting
        GreetingSection(profileName = uiState.activeProfile?.name ?: "User")

        Spacer(modifier = Modifier.height(20.dp))

        // First time / Welcome state
        if (uiState.isFirstTime) {
            WelcomeState(
                onAddMedication = onMedicationsClick,
                onAddCondition = onConditionsClick,
                onAddAppointment = onAppointmentsClick
            )
        } else {
            // Summary cards
            SummaryCardsRow(
                activeMedicationCount = uiState.activeMedicationCount,
                activeConditionCount = uiState.activeConditionCount,
                upcomingAppointmentCount = uiState.upcomingAppointmentCount,
                todayLogCount = uiState.todayLogCount,
                onMedicationsClick = onMedicationsClick,
                onConditionsClick = onConditionsClick,
                onAppointmentsClick = onAppointmentsClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions
            QuickActionsSection(
                onLogMedication = onLogMedication,
                onNewJournal = onNewJournal,
                onNewNote = onNewNote
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Upcoming Appointments
            if (uiState.upcomingAppointments.isNotEmpty()) {
                UpcomingAppointmentsSection(
                    appointments = uiState.upcomingAppointments,
                    onAppointmentClick = onAppointmentClick,
                    onViewAllClick = onAppointmentsClick
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Recent Activity
            if (uiState.recentActivity.isNotEmpty()) {
                RecentActivitySection(
                    activities = uiState.recentActivity,
                    onActivityClick = onActivityClick
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GreetingSection(profileName: String) {
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Text(
        text = "$greeting, $profileName",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun SummaryCardsRow(
    activeMedicationCount: Int,
    activeConditionCount: Int,
    upcomingAppointmentCount: Int,
    todayLogCount: Int,
    onMedicationsClick: () -> Unit,
    onConditionsClick: () -> Unit,
    onAppointmentsClick: () -> Unit
) {
    val cards = listOf(
        SummaryCardData("Medications", activeMedicationCount, Icons.Filled.Medication, onMedicationsClick),
        SummaryCardData("Conditions", activeConditionCount, Icons.Filled.Favorite, onConditionsClick),
        SummaryCardData("Appointments", upcomingAppointmentCount, Icons.Filled.CalendarToday, onAppointmentsClick),
        SummaryCardData("Today's Logs", todayLogCount, Icons.Filled.CheckCircle, null)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(end = 4.dp)
    ) {
        items(cards) { card ->
            SummaryCard(card = card)
        }
    }
}

private data class SummaryCardData(
    val label: String,
    val count: Int,
    val icon: ImageVector,
    val onClick: (() -> Unit)?
)

@Composable
private fun SummaryCard(card: SummaryCardData) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .then(
                if (card.onClick != null) Modifier.clickable(onClick = card.onClick)
                else Modifier
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = card.icon,
                contentDescription = card.label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = card.count.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = card.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onLogMedication: () -> Unit,
    onNewJournal: () -> Unit,
    onNewNote: () -> Unit
) {
    Text(
        text = "Quick Actions",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledTonalButton(
            onClick = onLogMedication,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Log Med", style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        FilledTonalButton(
            onClick = onNewJournal,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Icon(
                Icons.Filled.Book,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Journal", style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        FilledTonalButton(
            onClick = onNewNote,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Icon(
                Icons.Filled.NoteAdd,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Note", style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun UpcomingAppointmentsSection(
    appointments: List<com.medlog.app.data.local.entity.AppointmentEntity>,
    onAppointmentClick: (Long) -> Unit,
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Upcoming",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        TextButton(onClick = onViewAllClick) {
            Text("View all")
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    appointments.forEach { appointment ->
        AppointmentItem(
            appointment = appointment,
            onClick = { onAppointmentClick(appointment.id) }
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AppointmentItem(
    appointment: com.medlog.app.data.local.entity.AppointmentEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val dateStr = formatDateTime(appointment.appointmentDate)
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!appointment.doctorName.isNullOrBlank()) {
                    Text(
                        text = "Dr. ${appointment.doctorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!appointment.location.isNullOrBlank()) {
                    Text(
                        text = appointment.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentActivitySection(
    activities: List<RecentActivityItem>,
    onActivityClick: (RecentActivityItem) -> Unit
) {
    Text(
        text = "Recent Activity",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    )
    Spacer(modifier = Modifier.height(8.dp))
    activities.forEach { item ->
        ActivityItem(
            item = item,
            onClick = { onActivityClick(item) }
        )
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun ActivityItem(
    item: RecentActivityItem,
    onClick: () -> Unit
) {
    val icon = when (item.type) {
        "medication_log" -> Icons.Filled.Medication
        "condition" -> Icons.Filled.Favorite
        "journal" -> Icons.Filled.Book
        "appointment" -> Icons.Filled.CalendarToday
        else -> Icons.Filled.Info
    }
    val tint = when (item.type) {
        "medication_log" -> MaterialTheme.colorScheme.primary
        "condition" -> MaterialTheme.colorScheme.error
        "journal" -> MaterialTheme.colorScheme.tertiary
        "appointment" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = tint.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatRelativeTime(item.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WelcomeState(
    onAddMedication: () -> Unit,
    onAddCondition: () -> Unit,
    onAddAppointment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.LocalHospital,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to MedLog!",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start by adding your medications and conditions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onAddMedication) {
                    Icon(Icons.Filled.Medication, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Medication")
                }
                OutlinedButton(onClick = onAddCondition) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Condition")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onAddAppointment) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Appointment")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickLogMedicationDialog(
    medications: List<MedicationEntity>,
    onLog: (Long, String?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMedicationId by remember { mutableStateOf<Long?>(null) }
    var dosage by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Medication") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (medications.isEmpty()) {
                    Text(
                        text = "No active medications to log. Add a medication first.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // Medication selector
                    var expanded by remember { mutableStateOf(false) }
                    val selectedMed = medications.find { it.id == selectedMedicationId }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedMed?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Medication") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            medications.forEach { med ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(med.name, fontWeight = FontWeight.Medium)
                                            if (!med.dosage.isNullOrBlank()) {
                                                Text(
                                                    med.dosage,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedMedicationId = med.id
                                        dosage = med.dosage ?: ""
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage Taken") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedMedicationId?.let { id ->
                        onLog(id, dosage.ifBlank { null }, notes.ifBlank { null })
                    }
                },
                enabled = selectedMedicationId != null
            ) {
                Text("Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDateTime(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val days = ChronoUnit.DAYS.between(now.toLocalDate(), dateTime.toLocalDate())
    return when {
        days == 0L -> "Today at ${dateTime.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))}"
        days == 1L -> "Tomorrow at ${dateTime.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))}"
        days in 2..6 -> "${dateTime.toLocalDate().dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} at ${dateTime.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))}"
        else -> dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}

private fun formatRelativeTime(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(dateTime, now)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 1440 -> "${minutes / 60}h ago"
        minutes < 10080 -> "${minutes / 1440}d ago"
        else -> dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))
    }
}
