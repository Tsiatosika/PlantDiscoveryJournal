package com.example.plantdiscoveryjournal.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantdiscoveryjournal.data.repository.DiscoveryRepository
import com.example.plantdiscoveryjournal.domain.model.Discovery
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran liste du journal
 */
class JournalViewModel(
    private val repository: DiscoveryRepository,
    private val userId: String
) : ViewModel() {

    val discoveries: StateFlow<List<Discovery>> = repository
        .getAllDiscoveriesByUser(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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