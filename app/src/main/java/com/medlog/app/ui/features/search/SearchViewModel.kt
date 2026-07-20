package com.medlog.app.ui.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medlog.app.MedLogApp
import com.medlog.app.data.local.dao.SearchResults
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.data.repository.ProfileRepository
import com.medlog.app.data.repository.SearchRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: SearchResults = SearchResults(),
    val isSearching: Boolean = false,
    val activeProfile: ProfileEntity? = null,
    val error: String? = null
)

class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var currentProfileId: Long? = null

    init {
        viewModelScope.launch {
            profileRepository.getActiveProfile().collectLatest { profile ->
                currentProfileId = profile?.id
                _uiState.value = _uiState.value.copy(activeProfile = profile)
                // Re-run search if we have a query
                if (_uiState.value.query.isNotBlank()) {
                    performSearch(_uiState.value.query)
                }
            }
        }
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    fun onQueryChanged(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)

        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _uiState.value = _uiState.value.copy(
                results = SearchResults(),
                isSearching = false
            )
            return
        }

        searchJob = viewModelScope.launch {
            // Debounce 300ms
            kotlinx.coroutines.delay(300)
            performSearch(newQuery)
        }
    }

    private suspend fun performSearch(query: String) {
        val profileId = currentProfileId ?: return
        _uiState.value = _uiState.value.copy(isSearching = true)
        try {
            val results = searchRepository.searchAll(profileId, query.trim())
            _uiState.value = _uiState.value.copy(
                results = results,
                isSearching = false,
                error = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isSearching = false,
                error = e.message
            )
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            query = "",
            results = SearchResults(),
            isSearching = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val app: MedLogApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SearchViewModel(
                app.container.searchRepository,
                app.container.profileRepository
            ) as T
    }
}
