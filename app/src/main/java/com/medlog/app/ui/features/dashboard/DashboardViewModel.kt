package com.medlog.app.ui.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.entity.*
import com.medlog.app.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class RecentActivityItem(
    val id: Long,
    val type: String,
    val description: String,
    val date: LocalDateTime,
    val entityId: Long
)

data class DashboardUiState(
    val activeProfile: ProfileEntity? = null,
    val allProfiles: List<ProfileEntity> = emptyList(),
    val activeMedicationCount: Int = 0,
    val activeConditionCount: Int = 0,
    val upcomingAppointments: List<AppointmentEntity> = emptyList(),
    val todayLogCount: Int = 0,
    val recentActivity: List<RecentActivityItem> = emptyList(),
    val activeMedications: List<MedicationEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val upcomingAppointmentCount: Int get() = upcomingAppointments.size
    val isFirstTime: Boolean get() = activeProfile != null && activeMedicationCount == 0 && activeConditionCount == 0 && upcomingAppointments.isEmpty()
}

class DashboardViewModel(
    private val medicationRepository: MedicationRepository,
    private val conditionRepository: ConditionRepository,
    private val appointmentRepository: AppointmentRepository,
    private val journalRepository: JournalRepository,
    private val profileRepository: ProfileRepository,
    private val clutterRepository: ClutterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

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
                    allProfiles = allProfiles
                )
                if (activeProfile != null) {
                    loadDashboardData(activeProfile.id)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadDashboardData(profileId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                combine(
                    medicationRepository.getActiveMedicationsByProfile(profileId),
                    conditionRepository.getActiveConditionsByProfile(profileId),
                    appointmentRepository.getUpcomingAppointments(profileId),
                    medicationRepository.getTodayLogCount(profileId),
                    medicationRepository.getLogsForProfile(profileId),
                    journalRepository.getJournalByProfile(profileId),
                    appointmentRepository.getAppointmentsByProfile(profileId)
                ) { meds, conditions, upcoming, todayCount, medLogs, journals, allAppointments ->
                    val recentActivity = buildRecentActivity(medLogs, conditions, journals, upcoming)
                    DashboardUiState(
                        activeProfile = _uiState.value.activeProfile,
                        allProfiles = _uiState.value.allProfiles,
                        activeMedicationCount = meds.size,
                        activeConditionCount = conditions.size,
                        upcomingAppointments = upcoming.take(3),
                        todayLogCount = todayCount,
                        recentActivity = recentActivity,
                        activeMedications = meds,
                        isLoading = false,
                        error = null
                    )
                }.catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }.collect {
                    _uiState.value = it
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun buildRecentActivity(
        medLogs: List<MedicationLogEntity>,
        conditions: List<ConditionEntity>,
        journals: List<JournalEntryEntity>,
        appointments: List<AppointmentEntity>
    ): List<RecentActivityItem> {
        val items = mutableListOf<RecentActivityItem>()

        // Medication logs - take latest 5
        medLogs.sortedByDescending { it.takenAt }.take(5).forEach { log ->
            items.add(
                RecentActivityItem(
                    id = log.id,
                    type = "medication_log",
                    description = "Logged medication intake",
                    date = log.takenAt,
                    entityId = log.medicationId
                )
            )
        }

        // Condition updates - latest 5
        conditions.sortedByDescending { it.updatedAt }.take(5).forEach { condition ->
            items.add(
                RecentActivityItem(
                    id = condition.id,
                    type = "condition",
                    description = "${condition.name} updated",
                    date = condition.updatedAt,
                    entityId = condition.id
                )
            )
        }

        // Journal entries - latest 5
        journals.sortedByDescending { it.entryDate }.take(5).forEach { journal ->
            items.add(
                RecentActivityItem(
                    id = journal.id,
                    type = "journal",
                    description = journal.title ?: "Journal entry",
                    date = journal.entryDate,
                    entityId = journal.id
                )
            )
        }

        // Appointments - latest 5
        appointments.sortedByDescending { it.appointmentDate }.take(5).forEach { appointment ->
            items.add(
                RecentActivityItem(
                    id = appointment.id,
                    type = "appointment",
                    description = appointment.title,
                    date = appointment.appointmentDate,
                    entityId = appointment.id
                )
            )
        }

        return items.sortedByDescending { it.date }.take(5)
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

    fun logMedicationIntake(medicationId: Long, dosageTaken: String?, notes: String?) {
        val profileId = currentProfileId ?: return
        viewModelScope.launch {
            try {
                medicationRepository.logMedication(
                    medicationId = medicationId,
                    profileId = profileId,
                    dosageTaken = dosageTaken,
                    notes = notes
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val app: MedLogApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(
                app.container.medicationRepository,
                app.container.conditionRepository,
                app.container.appointmentRepository,
                app.container.journalRepository,
                app.container.profileRepository,
                app.container.clutterRepository
            ) as T
    }
}
