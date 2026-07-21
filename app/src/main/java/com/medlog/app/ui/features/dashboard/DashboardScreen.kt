package com.medlog.app.ui.features.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.medlog.app.ui.theme.GradientEnd
import com.medlog.app.ui.theme.GradientEndDark
import com.medlog.app.ui.theme.GradientStart
import com.medlog.app.ui.theme.GradientStartDark
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
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "No active profile",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create a profile to get started.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        FilledTonalButton(onClick = { navController.navigate(Route.Settings.route) }) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
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
    ) {
        // Gradient header with greeting
        GradientHeader(profileName = uiState.activeProfile?.name ?: "User")

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

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

                Spacer(modifier = Modifier.height(28.dp))

                // Quick Actions
                QuickActionsSection(
                    onLogMedication = onLogMedication,
                    onNewJournal = onNewJournal,
                    onNewNote = onNewNote
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Upcoming Appointments
                if (uiState.upcomingAppointments.isNotEmpty()) {
                    SectionHeader(title = "Upcoming", actionText = "View all", onAction = onAppointmentsClick)
                    Spacer(modifier = Modifier.height(12.dp))
                    uiState.upcomingAppointments.forEach { appointment ->
                        AppointmentItem(
                            appointment = appointment,
                            onClick = { onAppointmentClick(appointment.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }

                // Recent Activity
                if (uiState.recentActivity.isNotEmpty()) {
                    SectionHeader(title = "Recent Activity")
                    Spacer(modifier = Modifier.height(12.dp))
                    uiState.recentActivity.forEach { item ->
                        ActivityItem(
                            item = item,
                            onClick = { onActivityClick(item) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GradientHeader(profileName: String) {
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val dateText = remember {
        LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Text(
                text = "$greeting,",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = profileName,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionText)
            }
        }
    }
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
        SummaryCardData("Medications", activeMedicationCount, Icons.Filled.Medication, MaterialTheme.colorScheme.primary, onMedicationsClick),
        SummaryCardData("Conditions", activeConditionCount, Icons.Filled.Favorite, MaterialTheme.colorScheme.error, onConditionsClick),
        SummaryCardData("Upcoming", upcomingAppointmentCount, Icons.Filled.CalendarMonth, MaterialTheme.colorScheme.tertiary, onAppointmentsClick),
        SummaryCardData("Today", todayLogCount, Icons.Filled.CheckCircle, MaterialTheme.colorScheme.secondary, null)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(cards) { card ->
            AnimatedSummaryCard(card = card)
        }
    }
}

private data class SummaryCardData(
    val label: String,
    val count: Int,
    val icon: ImageVector,
    val accentColor: Color,
    val onClick: (() -> Unit)?
)

@Composable
private fun AnimatedSummaryCard(card: SummaryCardData) {
    val animatedCount by animateIntAsState(
        targetValue = card.count,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "count"
    )

    Card(
        modifier = Modifier
            .width(140.dp)
            .then(
                if (card.onClick != null) Modifier.clickable(onClick = card.onClick)
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = card.accentColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = card.icon,
                        contentDescription = card.label,
                        tint = card.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = animatedCount.toString(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp
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
    SectionHeader(title = "Quick Actions")
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            icon = Icons.Filled.Medication,
            label = "Log Med",
            color = MaterialTheme.colorScheme.primary,
            onClick = onLogMedication,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Filled.Book,
            label = "Journal",
            color = MaterialTheme.colorScheme.tertiary,
            onClick = onNewJournal,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Filled.NoteAdd,
            label = "Note",
            color = MaterialTheme.colorScheme.secondary,
            onClick = onNewNote,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        color = color.copy(alpha = 0.08f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
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
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
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
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
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
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = tint.copy(alpha = 0.1f)
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
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.LocalHospital,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onAddMedication) {
                    Icon(Icons.Filled.Medication, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Medication")
                }
                OutlinedButton(onClick = onAddCondition) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Condition")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onAddAppointment) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Appointment")
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
