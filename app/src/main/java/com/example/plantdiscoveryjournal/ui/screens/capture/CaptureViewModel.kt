package com.example.plantdiscoveryjournal.ui.screens.capture

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantdiscoveryjournal.data.repository.DiscoveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran de capture
 */
class CaptureViewModel(
    private val repository: DiscoveryRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<CaptureUiState>(CaptureUiState.Idle)
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _uiState.value = CaptureUiState.Processing("Sauvegarde de l'image...")

                // Sauvegarder l'image localement
                val imagePath = repository.saveImageLocally(bitmap, userId)

                _uiState.value = CaptureUiState.Processing("Identification en cours...")

                // Identifier avec l'IA
                val result = repository.identifyPlant(imagePath)

                result.onSuccess { identification ->
                    _uiState.value = CaptureUiState.Processing("Enregistrement de la découverte...")

                    // Sauvegarder dans la base de données
                    val discoveryId = repository.saveDiscovery(
                        userId = userId,
                        name = identification.name,
                        fact = identification.fact,
                        imagePath = imagePath,
                        timestamp = System.currentTimeMillis()
                    )

                    _uiState.value = CaptureUiState.Success(discoveryId)
                }.onFailure { error ->
                    _uiState.value = CaptureUiState.Error(
                        error.localizedMessage ?: "Erreur lors de l'identification"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = CaptureUiState.Error(
                    e.localizedMessage ?: "Erreur inconnue"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = CaptureUiState.Idle
    }
}

sealed class CaptureUiState {
    object Idle : CaptureUiState()
    data class Processing(val message: String) : CaptureUiState()
    data class Success(val discoveryId: Long) : CaptureUiState()
    data class Error(val message: String) : CaptureUiState()
}