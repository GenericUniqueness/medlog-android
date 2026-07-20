package com.medlog.app.ui.features.condition

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.entity.ConditionEntity
import com.medlog.app.data.local.entity.ConditionNoteEntity
import com.medlog.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ConditionUiState(
    val conditions: List<ConditionEntity> = emptyList(),
    val activeProfile: ProfileEntity? = null,
    val isLoading: Boolean = false,
    val selectedCondition: ConditionEntity? = null,
    val selectedConditionNotes: List<ConditionNoteEntity> = emptyList(),
    val error: String? = null,
    val statusFilter: ConditionStatusFilter = ConditionStatusFilter.ALL,
    val nameError: String? = null,
    val showDeleteDialog: Boolean = false,
    val conditionToDelete: ConditionEntity? = null,
    val noteToDelete: ConditionNoteEntity? = null,
    val showNoteDeleteDialog: Boolean = false
)

enum class ConditionStatusFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    MANAGED("Managed"),
    RESOLVED("Resolved")
}

class ConditionViewModel(application: Application) : AndroidViewModel(application) {

    private val conditionRepository = (application as MedLogApp).container.conditionRepository
    private val profileRepository = (application as MedLogApp).container.profileRepository

    private val _uiState = MutableStateFlow(ConditionUiState())
    val uiState: StateFlow<ConditionUiState> = _uiState.asStateFlow()

    private val _allConditions = MutableStateFlow<List<ConditionEntity>>(emptyList())

    init {
        loadActiveProfile()
    }

    private fun loadActiveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                profileRepository.getActiveProfile().collect { profile ->
                    _uiState.update { it.copy(activeProfile = profile, isLoading = false) }
                    if (profile != null) {
                        loadConditions(profile.id)
                    } else {
                        _uiState.update { it.copy(conditions = emptyList(), isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun loadConditions(profileId: Long) {
        viewModelScope.launch {
            try {
                conditionRepository.getConditionsByProfile(profileId).collect { conditions ->
                    _allConditions.value = conditions
                    applyFilter(_uiState.value.statusFilter, conditions)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun setStatusFilter(filter: ConditionStatusFilter) {
        _uiState.update { it.copy(statusFilter = filter) }
        applyFilter(filter, _allConditions.value)
    }

    private fun applyFilter(filter: ConditionStatusFilter, conditions: List<ConditionEntity>) {
        val filtered = when (filter) {
            ConditionStatusFilter.ALL -> conditions
            ConditionStatusFilter.ACTIVE -> conditions.filter { it.status.equals("active", ignoreCase = true) }
            ConditionStatusFilter.MANAGED -> conditions.filter { it.status.equals("managed", ignoreCase = true) }
            ConditionStatusFilter.RESOLVED -> conditions.filter { it.status.equals("resolved", ignoreCase = true) }
        }
        _uiState.update { it.copy(conditions = filtered) }
    }

    fun createCondition(
        name: String,
        severity: String?,
        diagnosedDate: LocalDate?,
        status: String,
        notes: String?
    ) {
        val profileId = _uiState.value.activeProfile?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                conditionRepository.createCondition(
                    profileId = profileId,
                    name = name,
                    severity = severity,
                    diagnosedDate = diagnosedDate,
                    status = status,
                    notes = notes
                )
                _uiState.update { it.copy(isLoading = false, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun updateCondition(condition: ConditionEntity) {
        viewModelScope.launch {
            try {
                conditionRepository.updateCondition(condition)
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteCondition(condition: ConditionEntity) {
        viewModelScope.launch {
            try {
                conditionRepository.deleteCondition(condition)
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        conditionToDelete = null,
                        selectedCondition = null,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, showDeleteDialog = false) }
            }
        }
    }

    fun selectCondition(conditionId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                conditionRepository.getConditionById(conditionId).collect { condition ->
                    _uiState.update { it.copy(selectedCondition = condition, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
        loadNotesForCondition(conditionId)
    }

    fun clearSelectedCondition() {
        _uiState.update { it.copy(selectedCondition = null, selectedConditionNotes = emptyList()) }
    }

    fun loadNotesForCondition(conditionId: Long) {
        viewModelScope.launch {
            try {
                conditionRepository.getNotesForCondition(conditionId).collect { notes ->
                    _uiState.update { it.copy(selectedConditionNotes = notes) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun addNote(conditionId: Long, content: String) {
        viewModelScope.launch {
            try {
                conditionRepository.addNote(conditionId, content)
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateNote(note: ConditionNoteEntity) {
        viewModelScope.launch {
            try {
                conditionRepository.updateNote(note)
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteNote(note: ConditionNoteEntity) {
        viewModelScope.launch {
            try {
                conditionRepository.deleteNote(note)
                _uiState.update { it.copy(showNoteDeleteDialog = false, noteToDelete = null, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, showNoteDeleteDialog = false) }
            }
        }
    }

    fun showDeleteConditionDialog(condition: ConditionEntity) {
        _uiState.update { it.copy(showDeleteDialog = true, conditionToDelete = condition) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false, conditionToDelete = null) }
    }

    fun showDeleteNoteDialog(note: ConditionNoteEntity) {
        _uiState.update { it.copy(showNoteDeleteDialog = true, noteToDelete = note) }
    }

    fun dismissNoteDeleteDialog() {
        _uiState.update { it.copy(showNoteDeleteDialog = false, noteToDelete = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun validateName(name: String): Boolean {
        val isValid = name.isNotBlank()
        _uiState.update { it.copy(nameError = if (isValid) null else "Condition name is required") }
        return isValid
    }

    class ConditionViewModelFactory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ConditionViewModel(application) as T
        }
    }
}
