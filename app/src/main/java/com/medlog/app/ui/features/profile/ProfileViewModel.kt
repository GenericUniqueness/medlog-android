package com.medlog.app.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ProfileUiState(
    val profiles: List<ProfileEntity> = emptyList(),
    val activeProfile: ProfileEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.getAllProfiles(), repository.getActiveProfile()) { profiles, active ->
                ProfileUiState(
                    profiles = profiles,
                    activeProfile = active,
                    isLoading = false
                )
            }.catch { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }.collect { _uiState.value = it }
        }
    }

    fun createProfile(
        name: String,
        dateOfBirth: LocalDate?,
        bloodType: String?,
        allergies: String?,
        notes: String?
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                val id = repository.createProfile(name, dateOfBirth, bloodType, allergies, notes)
                if (_uiState.value.activeProfile == null) {
                    repository.setActiveProfile(id)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun switchProfile(id: Long) {
        viewModelScope.launch {
            try {
                repository.setActiveProfile(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            try {
                repository.deleteProfile(profile)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            try {
                repository.updateProfile(profile)
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
            ProfileViewModel(app.container.profileRepository) as T
    }
}
