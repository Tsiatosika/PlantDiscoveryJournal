package com.example.plantdiscoveryjournal.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantdiscoveryjournal.data.repository.DiscoveryRepository
import com.example.plantdiscoveryjournal.domain.model.Discovery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran détail de découverte
 */
class DetailViewModel(
    private val repository: DiscoveryRepository,
    private val discoveryId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadDiscovery()
    }

    private fun loadDiscovery() {
        viewModelScope.launch {
            try {
                val discovery = repository.getDiscoveryById(discoveryId)
                _uiState.value = if (discovery != null) {
                    DetailUiState.Success(discovery)
                } else {
                    DetailUiState.Error("Découverte introuvable")
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(
                    e.localizedMessage ?: "Erreur de chargement"
                )
            }
        }
    }

    fun deleteDiscovery() {
        viewModelScope.launch {
            try {
                repository.deleteDiscovery(discoveryId)
                _uiState.value = DetailUiState.Deleted
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(
                    e.localizedMessage ?: "Erreur de suppression"
                )
            }
        }
    }
}

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val discovery: Discovery) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
    object Deleted : DetailUiState()
}