package com.medlog.app.ui.features.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medlog.app.data.repository.ProfileRepository
import com.medlog.app.data.repository.SettingsRepository
import com.medlog.app.ui.theme.GradientEnd
import com.medlog.app.ui.theme.GradientStart
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class OnboardingStep(val index: Int) {
    WELCOME(0),
    CREATE_PROFILE(1),
    FEATURE_OVERVIEW(2)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.WELCOME) }
    var profileName by remember { mutableStateOf("") }
    var profileNameError by remember { mutableStateOf<String?>(null) }
    var bloodType by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    // Animated visibility for step content
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(currentStep) {
        showContent = false
        kotlinx.coroutines.delay(50)
        showContent = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 40.dp)
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    // App icon
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.LocalHospital,
                                contentDescription = "MedLog",
                                modifier = Modifier.size(40.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Step content with fade animation
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState.index > initialState.index) {
                        slideInHorizontally { it / 3 } + fadeIn() togetherWith slideOutHorizontally { -it / 3 } + fadeOut()
                    } else {
                        slideInHorizontally { -it / 3 } + fadeIn() togetherWith slideOutHorizontally { it / 3 } + fadeOut()
                    }
                },
                modifier = Modifier.weight(1f),
                label = "onboarding_step"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (step) {
                        OnboardingStep.WELCOME -> WelcomeStep()
                        OnboardingStep.CREATE_PROFILE -> CreateProfileStep(
                            name = profileName,
                            onNameChange = {
                                profileName = it
                                profileNameError = if (it.isBlank()) "Name is required" else null
                            },
                            nameError = profileNameError,
                            bloodType = bloodType,
                            onBloodTypeChange = { bloodType = it },
                            allergies = allergies,
                            onAllergiesChange = { allergies = it },
                            bloodTypes = bloodTypes
                        )
                        OnboardingStep.FEATURE_OVERVIEW -> FeatureOverviewStep()
                    }
                }
            }

            // Bottom navigation
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    // Step indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OnboardingStep.entries.forEach { step ->
                            val isSelected = step == currentStep
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 24.dp else 8.dp,
                                animationSpec = tween(300),
                                label = "dot_width"
                            )
                            Surface(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(width),
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            ) {}
                            if (step != OnboardingStep.entries.last()) {
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Navigation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentStep != OnboardingStep.WELCOME) {
                            OutlinedButton(
                                onClick = {
                                    val prevIndex = currentStep.index - 1
                                    currentStep = OnboardingStep.entries[prevIndex]
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }
                        }

                        Button(
                            onClick = {
                                when (currentStep) {
                                    OnboardingStep.WELCOME -> {
                                        currentStep = OnboardingStep.CREATE_PROFILE
                                    }
                                    OnboardingStep.CREATE_PROFILE -> {
                                        if (profileName.isBlank()) {
                                            profileNameError = "Name is required"
                                            return@Button
                                        }
                                        currentStep = OnboardingStep.FEATURE_OVERVIEW
                                    }
                                    OnboardingStep.FEATURE_OVERVIEW -> {
                                        if (!isCreating) {
                                            isCreating = true
                                            scope.launch {
                                                try {
                                                    val profileId = profileRepository.createProfile(
                                                        name = profileName.trim(),
                                                        dateOfBirth = null,
                                                        bloodType = bloodType.ifBlank { null },
                                                        allergies = allergies.ifBlank { null },
                                                        notes = null
                                                    )
                                                    profileRepository.setActiveProfile(profileId)
                                                    settingsRepository.setSetting("onboarding_complete", "true")
                                                    onComplete()
                                                } catch (e: Exception) {
                                                    isCreating = false
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(if (currentStep == OnboardingStep.WELCOME) 1f else 1f),
                            enabled = when (currentStep) {
                                OnboardingStep.WELCOME -> true
                                OnboardingStep.CREATE_PROFILE -> profileName.isNotBlank()
                                OnboardingStep.FEATURE_OVERVIEW -> !isCreating
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isCreating && currentStep == OnboardingStep.FEATURE_OVERVIEW) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Creating\u2026")
                            } else {
                                Text(
                                    when (currentStep) {
                                        OnboardingStep.WELCOME -> "Get Started"
                                        OnboardingStep.CREATE_PROFILE -> "Continue"
                                        OnboardingStep.FEATURE_OVERVIEW -> "Start Using MedLog"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to MedLog",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your private, offline-first health journal. Track medications, conditions, appointments, and more\u2014all stored on your device.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        FeatureHighlight(icon = Icons.Filled.Shield, title = "Private & Secure", description = "All data stays on your device")
        Spacer(modifier = Modifier.height(12.dp))
        FeatureHighlight(icon = Icons.Filled.CloudOff, title = "Works Offline", description = "No internet connection required")
        Spacer(modifier = Modifier.height(12.dp))
        FeatureHighlight(icon = Icons.Filled.Group, title = "Multi-Profile", description = "Track health data for the whole family")
    }
}

@Composable
private fun FeatureHighlight(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateProfileStep(
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    bloodType: String,
    onBloodTypeChange: (String) -> Unit,
    allergies: String,
    onAllergiesChange: (String) -> Unit,
    bloodTypes: List<String>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section header with icon
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Create Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tell us a bit about yourself to get started.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name *") },
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Filled.Person, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                leadingIcon = { Icon(Icons.Filled.Bloodtype, contentDescription = null) },
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
                        onBloodTypeChange("")
                        bloodTypeExpanded = false
                    }
                )
                bloodTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            onBloodTypeChange(type)
                            bloodTypeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Allergies field
        OutlinedTextField(
            value = allergies,
            onValueChange = onAllergiesChange,
            label = { Text("Allergies (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            leadingIcon = {
                Icon(Icons.Filled.Warning, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FeatureOverviewStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "What You Can Do",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "MedLog helps you keep track of every aspect of your health journey.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        FeatureCard(
            icon = Icons.Filled.Medication,
            title = "Track Medications",
            description = "Log intake, view history, set reminders",
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureCard(
            icon = Icons.Filled.Favorite,
            title = "Monitor Conditions",
            description = "Track symptoms, add notes over time",
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureCard(
            icon = Icons.Filled.CalendarMonth,
            title = "Manage Appointments",
            description = "Schedule visits, set reminders",
            color = MaterialTheme.colorScheme.tertiary
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureCard(
            icon = Icons.Filled.Book,
            title = "Keep a Journal",
            description = "Record how you feel, track your mood",
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = color
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
