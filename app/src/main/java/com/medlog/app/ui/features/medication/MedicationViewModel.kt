package com.medlog.app.ui.features.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.entity.MedicationChangeEntity
import com.medlog.app.data.local.entity.MedicationEntity
import com.medlog.app.data.local.entity.MedicationLogEntity
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.data.repository.MedicationRepository
import com.medlog.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class MedicationStatusFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    PAUSED("Paused"),
    DISCONTINUED("Discontinued")
}

data class MedicationListUiState(
    val medications: List<MedicationEntity> = emptyList(),
    val activeProfile: ProfileEntity? = null,
    val allProfiles: List<ProfileEntity> = emptyList(),
    val statusFilter: MedicationStatusFilter = MedicationStatusFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val filteredMedications: List<MedicationEntity>
        get() {
            var result = medications

            // Apply status filter
            result = when (statusFilter) {
                MedicationStatusFilter.ALL -> result
                MedicationStatusFilter.ACTIVE -> result.filter { it.status == "active" }
                MedicationStatusFilter.PAUSED -> result.filter { it.status == "paused" }
                MedicationStatusFilter.DISCONTINUED -> result.filter { it.status == "discontinued" }
            }

            // Apply search query
            if (searchQuery.isNotBlank()) {
                val query = searchQuery.lowercase()
                result = result.filter {
                    it.name.lowercase().contains(query) ||
                        it.dosage?.lowercase()?.contains(query) == true
                }
            }

            return result
        }
}

data class MedicationDetailUiState(
    val medication: MedicationEntity? = null,
    val logs: List<MedicationLogEntity> = emptyList(),
    val changes: List<MedicationChangeEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class MedicationViewModel(
    private val medicationRepository: MedicationRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationListUiState())
    val uiState: StateFlow<MedicationListUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow(MedicationDetailUiState())
    val detailState: StateFlow<MedicationDetailUiState> = _detailState.asStateFlow()

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
                    loadMedications(activeProfile.id)
                } else {
                    _uiState.value = _uiState.value.copy(
                        medications = emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadMedications(profileId: Long) {
        viewModelScope.launch {
            medicationRepository.getMedicationsByProfile(profileId).catch { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }.collect { medications ->
                _uiState.value = _uiState.value.copy(
                    medications = medications,
                    isLoading = false
                )
            }
        }
    }

    fun setStatusFilter(filter: MedicationStatusFilter) {
        _uiState.value = _uiState.value.copy(statusFilter = filter)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun createMedication(
        name: String,
        dosage: String?,
        frequency: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        status: String,
        notes: String?
    ) {
        if (name.isBlank()) return
        val profileId = currentProfileId ?: return
        viewModelScope.launch {
            try {
                val id = medicationRepository.createMedication(
                    profileId = profileId,
                    name = name.trim(),
                    dosage = dosage?.trim()?.ifBlank { null },
                    frequency = frequency?.trim()?.ifBlank { null },
                    startDate = startDate,
                    endDate = endDate,
                    status = status,
                    notes = notes?.trim()?.ifBlank { null }
                )
                // Record initial change if this is a new medication
                medicationRepository.recordChange(
                    medicationId = id,
                    changeType = "created",
                    previousValue = null,
                    newValue = name.trim(),
                    reason = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            try {
                val old = medicationRepository.getMedicationById(medication.id).first()
                medicationRepository.updateMedication(medication)

                // Record relevant changes
                if (old != null) {
                    if (old.dosage != medication.dosage) {
                        medicationRepository.recordChange(
                            medicationId = medication.id,
                            changeType = "dosage_change",
                            previousValue = old.dosage,
                            newValue = medication.dosage,
                            reason = null
                        )
                    }
                    if (old.frequency != medication.frequency) {
                        medicationRepository.recordChange(
                            medicationId = medication.id,
                            changeType = "frequency_change",
                            previousValue = old.frequency,
                            newValue = medication.frequency,
                            reason = null
                        )
                    }
                    if (old.status != medication.status) {
                        medicationRepository.recordChange(
                            medicationId = medication.id,
                            changeType = "status_change",
                            previousValue = old.status,
                            newValue = medication.status,
                            reason = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            try {
                medicationRepository.deleteMedication(medication)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun logMedicationIntake(
        medicationId: Long,
        dosageTaken: String?,
        notes: String?
    ) {
        val profileId = currentProfileId ?: return
        viewModelScope.launch {
            try {
                medicationRepository.logMedication(
                    medicationId = medicationId,
                    profileId = profileId,
                    dosageTaken = dosageTaken?.trim()?.ifBlank { null },
                    notes = notes?.trim()?.ifBlank { null }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun recordMedicationChange(
        medicationId: Long,
        changeType: String,
        previousValue: String?,
        newValue: String?,
        reason: String?
    ) {
        viewModelScope.launch {
            try {
                medicationRepository.recordChange(
                    medicationId = medicationId,
                    changeType = changeType,
                    previousValue = previousValue?.ifBlank { null },
                    newValue = newValue?.ifBlank { null },
                    reason = reason?.ifBlank { null }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun loadMedicationDetail(medicationId: Long) {
        viewModelScope.launch {
            _detailState.value = MedicationDetailUiState(isLoading = true)
            try {
                combine(
                    medicationRepository.getMedicationById(medicationId),
                    medicationRepository.getLogsForMedication(medicationId),
                    medicationRepository.getChangesForMedication(medicationId)
                ) { medication, logs, changes ->
                    MedicationDetailUiState(
                        medication = medication,
                        logs = logs,
                        changes = changes,
                        isLoading = false
                    )
                }.catch { e ->
                    _detailState.value = MedicationDetailUiState(
                        isLoading = false,
                        error = e.message
                    )
                }.collect {
                    _detailState.value = it
                }
            } catch (e: Exception) {
                _detailState.value = MedicationDetailUiState(
                    isLoading = false,
                    error = e.message
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

    fun clearListError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearDetailError() {
        _detailState.value = _detailState.value.copy(error = null)
    }

    class Factory(private val app: MedLogApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MedicationViewModel(
                app.container.medicationRepository,
                app.container.profileRepository
            ) as T
    }
}
