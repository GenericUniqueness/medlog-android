package com.medlog.app.ui.features.settings

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.entity.AppointmentEntity
import com.medlog.app.data.local.entity.ConditionEntity
import com.medlog.app.data.local.entity.JournalEntryEntity
import com.medlog.app.data.local.entity.MedicationEntity
import com.medlog.app.data.local.entity.MedicationLogEntity
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SettingsUiState(
    val profiles: List<ProfileEntity> = emptyList(),
    val activeProfile: ProfileEntity? = null,
    val medicationRemindersEnabled: Boolean = true,
    val appointmentRemindersEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val error: String? = null,
    val exportSuccess: Boolean = false
)

class SettingsViewModel(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val medicationRepository: MedicationRepository,
    private val conditionRepository: ConditionRepository,
    private val appointmentRepository: AppointmentRepository,
    private val journalRepository: JournalRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var currentProfileId: Long? = null

    init {
        viewModelScope.launch {
            combine(
                profileRepository.getActiveProfile(),
                profileRepository.getAllProfiles()
            ) { activeProfile, allProfiles ->
                activeProfile to allProfiles
            }.collectLatest { (activeProfile, allProfiles) ->
                currentProfileId = activeProfile?.id
                _uiState.value = _uiState.value.copy(
                    activeProfile = activeProfile,
                    profiles = allProfiles,
                    isLoading = false
                )
            }
        }

        // Load notification settings
        viewModelScope.launch {
            settingsRepository.getSetting("medication_reminders_enabled").collectLatest { setting ->
                _uiState.value = _uiState.value.copy(
                    medicationRemindersEnabled = setting?.value != "false"
                )
            }
        }
        viewModelScope.launch {
            settingsRepository.getSetting("appointment_reminders_enabled").collectLatest { setting ->
                _uiState.value = _uiState.value.copy(
                    appointmentRemindersEnabled = setting?.value != "false"
                )
            }
        }
    }

    fun switchProfile(profileId: Long) {
        viewModelScope.launch {
            try {
                profileRepository.setActiveProfile(profileId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun createProfile(
        name: String,
        dateOfBirth: LocalDate?,
        bloodType: String?,
        allergies: String?
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                val id = profileRepository.createProfile(
                    name = name.trim(),
                    dateOfBirth = dateOfBirth,
                    bloodType = bloodType?.trim()?.ifBlank { null },
                    allergies = allergies?.trim()?.ifBlank { null },
                    notes = null
                )
                profileRepository.setActiveProfile(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            try {
                profileRepository.updateProfile(profile)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            try {
                profileRepository.deleteProfile(profile)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun setMedicationRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setSetting("medication_reminders_enabled", enabled.toString())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun setAppointmentRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setSetting("appointment_reminders_enabled", enabled.toString())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun exportDataAsPdf() {
        val profileId = currentProfileId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val profile = profileRepository.getActiveProfile().first()
                val medications = medicationRepository.getActiveMedicationsByProfile(profileId).first()
                val conditions = conditionRepository.getActiveConditionsByProfile(profileId).first()
                val appointments = appointmentRepository.getUpcomingAppointments(profileId).first()
                val journals = journalRepository.getJournalByProfile(profileId).first()

                val medLogsMap = mutableMapOf<Long, List<MedicationLogEntity>>()
                for (med in medications) {
                    medLogsMap[med.id] = medicationRepository.getLogsForMedication(med.id).first().take(5)
                }

                val pdfGenerator = PdfReportGenerator(context)
                val file = pdfGenerator.generateReport(
                    profile = profile,
                    medications = medications,
                    medLogsMap = medLogsMap,
                    conditions = conditions,
                    appointments = appointments,
                    journals = journals.take(10)
                )

                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = e.message
                )
            }
        }
    }

    fun clearExportSuccess() {
        _uiState.value = _uiState.value.copy(exportSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val app: MedLogApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(
                app.container.profileRepository,
                app.container.settingsRepository,
                app.container.medicationRepository,
                app.container.conditionRepository,
                app.container.appointmentRepository,
                app.container.journalRepository,
                app
            ) as T
    }
}
