package com.medlog.app.ui.features.appointment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.entity.AppointmentEntity
import com.medlog.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class AppointmentUiState(
    val appointments: List<AppointmentEntity> = emptyList(),
    val activeProfile: ProfileEntity? = null,
    val isLoading: Boolean = false,
    val selectedAppointment: AppointmentEntity? = null,
    val error: String? = null,
    val statusFilter: AppointmentStatusFilter = AppointmentStatusFilter.ALL,
    val titleError: String? = null,
    val showDeleteDialog: Boolean = false,
    val appointmentToDelete: AppointmentEntity? = null
)

enum class AppointmentStatusFilter(val label: String) {
    ALL("All"),
    UPCOMING("Upcoming"),
    PAST("Past"),
    CANCELLED("Cancelled")
}

class AppointmentViewModel(application: Application) : AndroidViewModel(application) {

    private val appointmentRepository = (application as MedLogApp).container.appointmentRepository
    private val profileRepository = (application as MedLogApp).container.profileRepository

    private val _uiState = MutableStateFlow(AppointmentUiState())
    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    private val _allAppointments = MutableStateFlow<List<AppointmentEntity>>(emptyList())

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
                        loadAppointments(profile.id)
                    } else {
                        _uiState.update { it.copy(appointments = emptyList(), isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun loadAppointments(profileId: Long) {
        viewModelScope.launch {
            try {
                appointmentRepository.getAppointmentsByProfile(profileId).collect { appointments ->
                    _allAppointments.value = appointments
                    applyFilter(_uiState.value.statusFilter, appointments)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun setStatusFilter(filter: AppointmentStatusFilter) {
        _uiState.update { it.copy(statusFilter = filter) }
        applyFilter(filter, _allAppointments.value)
    }

    private fun applyFilter(filter: AppointmentStatusFilter, appointments: List<AppointmentEntity>) {
        val now = LocalDateTime.now()
        val filtered = when (filter) {
            AppointmentStatusFilter.ALL -> appointments
            AppointmentStatusFilter.UPCOMING -> appointments.filter {
                it.appointmentDate.isAfter(now) && it.status.equals("scheduled", ignoreCase = true)
            }
            AppointmentStatusFilter.PAST -> appointments.filter {
                it.appointmentDate.isBefore(now) || it.status.equals("completed", ignoreCase = true)
            }
            AppointmentStatusFilter.CANCELLED -> appointments.filter {
                it.status.equals("cancelled", ignoreCase = true)
            }
        }
        _uiState.update { it.copy(appointments = filtered) }
    }

    fun createAppointment(
        title: String,
        doctorName: String?,
        location: String?,
        appointmentDate: LocalDateTime,
        duration: Int?,
        notes: String?,
        status: String,
        reminderEnabled: Boolean
    ) {
        val profileId = _uiState.value.activeProfile?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                appointmentRepository.createAppointment(
                    profileId = profileId,
                    title = title,
                    doctorName = doctorName,
                    location = location,
                    appointmentDate = appointmentDate,
                    duration = duration,
                    notes = notes,
                    status = status,
                    reminderEnabled = reminderEnabled
                )
                _uiState.update { it.copy(isLoading = false, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun updateAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            try {
                appointmentRepository.updateAppointment(appointment)
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            try {
                appointmentRepository.deleteAppointment(appointment)
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        appointmentToDelete = null,
                        selectedAppointment = null,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, showDeleteDialog = false) }
            }
        }
    }

    fun getAppointmentById(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                appointmentRepository.getAppointmentById(id).collect { appointment ->
                    _uiState.update { it.copy(selectedAppointment = appointment, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun markAsCompleted(appointment: AppointmentEntity) {
        viewModelScope.launch {
            try {
                appointmentRepository.updateAppointment(
                    appointment.copy(status = "completed")
                )
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun showDeleteDialog(appointment: AppointmentEntity) {
        _uiState.update { it.copy(showDeleteDialog = true, appointmentToDelete = appointment) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false, appointmentToDelete = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun validateTitle(title: String): Boolean {
        val isValid = title.isNotBlank()
        _uiState.update { it.copy(titleError = if (isValid) null else "Appointment title is required") }
        return isValid
    }

    class AppointmentViewModelFactory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return AppointmentViewModel(application) as T
        }
    }
}
