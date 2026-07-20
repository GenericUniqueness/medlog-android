package com.medlog.app.ui.features.section

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.data.local.entity.SectionEntity
import com.medlog.app.data.local.entity.SectionEntryEntity
import com.medlog.app.data.repository.ProfileRepository
import com.medlog.app.data.repository.SectionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SectionWithEntries(
    val section: SectionEntity,
    val entries: List<SectionEntryEntity> = emptyList()
)

data class SectionListUiState(
    val sectionsWithEntries: List<SectionWithEntries> = emptyList(),
    val activeProfile: ProfileEntity? = null,
    val allProfiles: List<ProfileEntity> = emptyList(),
    val expandedSectionIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class SectionDetailUiState(
    val section: SectionEntity? = null,
    val entries: List<SectionEntryEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class SectionViewModel(application: Application) : AndroidViewModel(application) {

    private val sectionRepository: SectionRepository =
        (application as MedLogApp).container.sectionRepository
    private val profileRepository: ProfileRepository =
        (application as MedLogApp).container.profileRepository

    private val _uiState = MutableStateFlow(SectionListUiState())
    val uiState: StateFlow<SectionListUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow(SectionDetailUiState())
    val detailState: StateFlow<SectionDetailUiState> = _detailState.asStateFlow()

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
                    loadSections(activeProfile.id)
                } else {
                    _uiState.value = _uiState.value.copy(
                        sectionsWithEntries = emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadSections(profileId: Long) {
        viewModelScope.launch {
            sectionRepository.getSectionsByProfile(profileId).catch { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }.collect { sections ->
                // Load entries for each section
                val sectionsWithEntries = mutableListOf<SectionWithEntries>()
                for (section in sections) {
                    val entries = try {
                        sectionRepository.getEntriesForSection(section.id).first()
                    } catch (_: Exception) {
                        emptyList()
                    }
                    sectionsWithEntries.add(SectionWithEntries(section, entries))
                }
                _uiState.value = _uiState.value.copy(
                    sectionsWithEntries = sectionsWithEntries,
                    isLoading = false
                )
            }
        }
    }

    fun toggleSectionExpanded(sectionId: Long) {
        val current = _uiState.value.expandedSectionIds
        _uiState.value = _uiState.value.copy(
            expandedSectionIds = if (sectionId in current) {
                current - sectionId
            } else {
                current + sectionId
            }
        )
    }

    fun createSection(title: String) {
        if (title.isBlank()) return
        val profileId = currentProfileId ?: return
        viewModelScope.launch {
            try {
                val sortOrder = _uiState.value.sectionsWithEntries.size
                sectionRepository.createSection(
                    profileId = profileId,
                    title = title.trim(),
                    sortOrder = sortOrder
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateSection(section: SectionEntity) {
        viewModelScope.launch {
            try {
                sectionRepository.updateSection(section)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteSection(section: SectionEntity) {
        viewModelScope.launch {
            try {
                sectionRepository.deleteSection(section)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun addEntry(sectionId: Long, title: String, content: String?) {
        if (title.isBlank()) return
        viewModelScope.launch {
            try {
                val sortOrder = try {
                    sectionRepository.getEntriesForSection(sectionId).first().size
                } catch (_: Exception) {
                    0
                }
                sectionRepository.addEntry(
                    sectionId = sectionId,
                    title = title.trim(),
                    content = content?.trim()?.ifBlank { null },
                    sortOrder = sortOrder
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateEntry(entry: SectionEntryEntity) {
        viewModelScope.launch {
            try {
                sectionRepository.updateEntry(entry)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteEntry(entry: SectionEntryEntity) {
        viewModelScope.launch {
            try {
                sectionRepository.deleteEntry(entry)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun loadSectionDetail(sectionId: Long) {
        viewModelScope.launch {
            _detailState.value = SectionDetailUiState(isLoading = true)
            try {
                combine(
                    sectionRepository.getSectionById(sectionId),
                    sectionRepository.getEntriesForSection(sectionId)
                ) { section, entries ->
                    SectionDetailUiState(
                        section = section,
                        entries = entries,
                        isLoading = false
                    )
                }.catch { e ->
                    _detailState.value = SectionDetailUiState(
                        isLoading = false,
                        error = e.message
                    )
                }.collect {
                    _detailState.value = it
                }
            } catch (e: Exception) {
                _detailState.value = SectionDetailUiState(
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

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            SectionViewModel(app) as T
    }
}
