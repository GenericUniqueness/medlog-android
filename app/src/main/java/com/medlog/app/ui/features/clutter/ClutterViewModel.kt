package com.medlog.app.ui.features.clutter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.entity.ClutterItemEntity
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.data.repository.ClutterRepository
import com.medlog.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ClutterUiState(
    val items: List<ClutterItemEntity> = emptyList(),
    val activeProfile: ProfileEntity? = null,
    val allProfiles: List<ProfileEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ClutterViewModel(application: Application) : AndroidViewModel(application) {

    private val clutterRepository: ClutterRepository =
        (application as MedLogApp).container.clutterRepository
    private val profileRepository: ProfileRepository =
        (application as MedLogApp).container.profileRepository

    private val _uiState = MutableStateFlow(ClutterUiState())
    val uiState: StateFlow<ClutterUiState> = _uiState.asStateFlow()

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
                    loadItems(activeProfile.id)
                } else {
                    _uiState.value = _uiState.value.copy(
                        items = emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadItems(profileId: Long) {
        viewModelScope.launch {
            clutterRepository.getClutterByProfile(profileId).catch { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }.collect { items ->
                _uiState.value = _uiState.value.copy(
                    items = items,
                    isLoading = false
                )
            }
        }
    }

    fun addItem(content: String) {
        if (content.isBlank()) return
        val profileId = currentProfileId ?: return
        viewModelScope.launch {
            try {
                clutterRepository.addClutterItem(
                    profileId = profileId,
                    content = content.trim()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateItem(item: ClutterItemEntity) {
        viewModelScope.launch {
            try {
                clutterRepository.updateClutterItem(item)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteItem(item: ClutterItemEntity) {
        viewModelScope.launch {
            try {
                clutterRepository.deleteClutterItem(item)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            ClutterViewModel(app) as T
    }
}
