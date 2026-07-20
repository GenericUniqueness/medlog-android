package com.medlog.app.ui.features.journal

import android.app.DatePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.medlog.app.data.local.entity.JournalEntryEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private data class MoodOption(
    val value: String,
    val emoji: String,
    val label: String
)

private val moodOptions = listOf(
    MoodOption("great", "😊", "Great"),
    MoodOption("good", "🙂", "Good"),
    MoodOption("okay", "😐", "Okay"),
    MoodOption("bad", "😞", "Bad"),
    MoodOption("terrible", "😢", "Terrible")
)

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJournalEntryScreen(navController: NavHostController) {
    val viewModel: JournalViewModel = viewModel(factory = JournalViewModel.Factory(
        navController.context.applicationContext as android.app.Application
    ))
    val uiState by viewModel.uiState.collectAsState()

    // Check if we are editing by looking for a saved entry ID from detail screen
    val editEntryId = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<Long>("editEntryId")

    // Form state
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var contentError by remember { mutableStateOf<String?>(null) }
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var entryDate by remember { mutableStateOf(LocalDate.now()) }
    val context = LocalContext.current

    // If editing, load existing entry data
    var loadedEditEntry by remember { mutableStateOf<JournalEntryEntity?>(null) }
    LaunchedEffect(editEntryId) {
        if (editEntryId != null && editEntryId > 0) {
            viewModel.loadEntryDetail(editEntryId)
        }
    }
    val detailState by viewModel.detailState.collectAsState()
    LaunchedEffect(detailState.entry) {
        if (editEntryId != null && detailState.entry != null && loadedEditEntry == null) {
            loadedEditEntry = detailState.entry
            title = detailState.entry!!.title ?: ""
            content = detailState.entry!!.content
            selectedMood = detailState.entry!!.mood
            entryDate = detailState.entry!!.entryDate.toLocalDate()
        }
    }

    val isEditing = editEntryId != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Journal Entry" else "New Journal Entry") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title (optional)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Content (required)
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    contentError = if (it.isBlank()) "Content is required" else null
                },
                label = { Text("Content *") },
                minLines = 4,
                maxLines = 10,
                isError = contentError != null,
                supportingText = contentError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Mood selector
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                moodOptions.forEach { option ->
                    MoodButton(
                        emoji = option.emoji,
                        label = option.label,
                        isSelected = selectedMood == option.value,
                        onClick = {
                            selectedMood = if (selectedMood == option.value) null else option.value
                        }
                    )
                }
            }

            // Date picker
            OutlinedTextField(
                value = entryDate.format(dateFormatter),
                onValueChange = {},
                label = { Text("Entry Date") },
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = {
                        val today = LocalDate.now()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                entryDate = LocalDate.of(year, month + 1, day)
                            },
                            entryDate.year,
                            entryDate.monthValue - 1,
                            entryDate.dayOfMonth
                        ).show()
                    }) {
                        Text("Pick")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    if (content.isBlank()) {
                        contentError = "Content is required"
                        return@Button
                    }
                    if (isEditing && loadedEditEntry != null) {
                        viewModel.updateEntry(
                            loadedEditEntry!!.copy(
                                title = title.trim().ifBlank { null },
                                content = content.trim(),
                                mood = selectedMood,
                                entryDate = entryDate.atTime(
                                    loadedEditEntry!!.entryDate.hour,
                                    loadedEditEntry!!.entryDate.minute
                                )
                            )
                        )
                    } else {
                        viewModel.createEntry(
                            title = title.trim().ifBlank { null },
                            content = content.trim(),
                            mood = selectedMood
                        )
                    }
                    navController.popBackStack()
                },
                enabled = content.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Save Changes" else "Save Entry")
            }
        }
    }
}

@Composable
private fun MoodButton(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            )
            .padding(8.dp)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
