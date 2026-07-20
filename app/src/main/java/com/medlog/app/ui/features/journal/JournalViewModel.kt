package com.medlog.app.ui.features.journal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.entity.JournalEntryEntity
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.data.repository.JournalRepository
import com.medlog.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MoodFilter(val label: String, val value: String?) {
    ALL("All", null),
    GREAT("Great", "great"),
    GOOD("Good", "good"),
    OKAY("Okay", "okay"),
    BAD("Bad", "bad"),
    TERRIBLE("Terrible", "terrible")
}

data class JournalListUiState(
    val entries: List<JournalEntryEntity> = emptyList(),
    val activeProfile: ProfileEntity? = null,
    val allProfiles: List<ProfileEntity> = emptyList(),
    val moodFilter: MoodFilter = MoodFilter.ALL,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val filteredEntries: List<JournalEntryEntity>
        get() {
            return if (moodFilter == MoodFilter.ALL) {
                entries
            } else {
                entries.filter { it.mood == moodFilter.value }
            }
        }
}

data class JournalDetailUiState(
    val entry: JournalEntryEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val journalRepository: JournalRepository =
        (application as MedLogApp).container.journalRepository
    private val profileRepository: ProfileRepository =
        (application as MedLogApp).container.profileRepository

    private val _uiState = MutableStateFlow(JournalListUiState())
    val uiState: StateFlow<JournalListUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow(JournalDetailUiState())
    val detailState: StateFlow<JournalDetailUiState> = _detailState.asStateFlow()

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
                    loadEntries(activeProfile.id)
                } else {
                    _uiState.value = _uiState.value.copy(
                        entries = emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadEntries(profileId: Long) {
        viewModelScope.launch {
            journalRepository.getJournalByProfile(profileId).catch { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }.collect { entries ->
                _uiState.value = _uiState.value.copy(
                    entries = entries,
                    isLoading = false
                )
            }
        }
    }

    fun setMoodFilter(filter: MoodFilter) {
        _uiState.value = _uiState.value.copy(moodFilter = filter)
    }

    fun createEntry(title: String?, content: String, mood: String?) {
        if (content.isBlank()) return
        val profileId = currentProfileId ?: return
        viewModelScope.launch {
            try {
                journalRepository.createEntry(
                    profileId = profileId,
                    title = title?.trim()?.ifBlank { null },
                    content = content.trim(),
                    mood = mood
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateEntry(entry: JournalEntryEntity) {
        viewModelScope.launch {
            try {
                journalRepository.updateEntry(entry)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteEntry(entry: JournalEntryEntity) {
        viewModelScope.launch {
            try {
                journalRepository.deleteEntry(entry)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun loadEntryDetail(entryId: Long) {
        viewModelScope.launch {
            _detailState.value = JournalDetailUiState(isLoading = true)
            try {
                journalRepository.getJournalEntryById(entryId).catch { e ->
                    _detailState.value = JournalDetailUiState(
                        isLoading = false,
                        error = e.message
                    )
                }.collect { entry ->
                    _detailState.value = JournalDetailUiState(
                        entry = entry,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _detailState.value = JournalDetailUiState(
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
            JournalViewModel(app) as T
    }
}
