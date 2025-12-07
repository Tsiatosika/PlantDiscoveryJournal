package com.example.plantdiscoveryjournal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantdiscoveryjournal.data.repository.DiscoveryRepository
import com.example.plantdiscoveryjournal.domain.model.Discovery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Options de tri pour le journal
enum class JournalSortOption {
    DATE_DESC,   // plus récent d'abord
    DATE_ASC,
    NAME_ASC,
    NAME_DESC
}

/**
 * ViewModel pour l'écran liste du journal
 */
class JournalViewModel(
    private val repository: DiscoveryRepository,
    private val userId: String
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    // Option de tri
    private val _sortOption = MutableStateFlow(JournalSortOption.DATE_DESC)
    val sortOption: StateFlow<JournalSortOption> = _sortOption.asStateFlow()

    private val allDiscoveries = repository
        .getAllDiscoveriesByUser(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val discoveries: StateFlow<List<Discovery>> = combine(
        allDiscoveries,
        _searchQuery,
        _sortOption
    ) { discoveries, query, sort ->
        val filtered = if (query.isBlank()) {
            discoveries
        } else {
            discoveries.filter { discovery ->
                discovery.name.contains(query, ignoreCase = true)
                // si tu ajoutes location/notes dans Discovery, tu pourras filtrer aussi dessus
                // || discovery.location.contains(query, ignoreCase = true)
                // || discovery.notes.contains(query, ignoreCase = true)
            }
        }

        when (sort) {
            JournalSortOption.DATE_DESC -> filtered.sortedByDescending { it.timestamp }
            JournalSortOption.DATE_ASC -> filtered.sortedBy { it.timestamp }
            JournalSortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            JournalSortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _isSearchActive.value = false
    }

    fun setSortOption(option: JournalSortOption) {
        _sortOption.value = option
    }

    fun deleteDiscovery(discoveryId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteDiscovery(discoveryId)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Erreur lors de la suppression: ${e.localizedMessage}"
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
