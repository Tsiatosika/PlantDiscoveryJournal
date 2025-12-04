package com.example.plantdiscoveryjournal.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantdiscoveryjournal.data.repository.DiscoveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel pour l’écran de capture
class CaptureViewModel(
    private val repository: DiscoveryRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<CaptureUiState>(CaptureUiState.Idle)
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    fun processImage(bitmap: Bitmap, category: String) {
        viewModelScope.launch {
            try {
                // 1) Sauvegarde de l’image
                _uiState.value = CaptureUiState.Processing(
                    message = "Saving image...",
                    capturedImage = bitmap,
                    progress = 0.2f
                )

                val imagePath = repository.saveImageLocally(bitmap, userId)

                // 2) Analyse IA
                _uiState.value = CaptureUiState.Processing(
                    message = "Analyzing with AI...",
                    capturedImage = bitmap,
                    progress = 0.5f
                )

                val result = repository.identifyPlant(imagePath)

                result.onSuccess { identification ->
                    // ⚠️ Ne pas enregistrer si l’objet n’est pas une plante identifiable
                    if (identification.name.contains("Objet non identifiable", ignoreCase = true)) {
                        _uiState.value = CaptureUiState.Error(
                            message = "Impossible d'identifier une plante sur cette image.",
                            capturedImage = bitmap
                        )
                        return@onSuccess
                    }

                    // 3) Sauvegarde de la découverte
                    _uiState.value = CaptureUiState.Processing(
                        message = "Saving discovery...",
                        capturedImage = bitmap,
                        progress = 0.8f
                    )

                    val discoveryId = repository.saveDiscovery(
                        userId = userId,
                        name = identification.name,
                        fact = identification.fact,
                        imagePath = imagePath,
                        timestamp = System.currentTimeMillis(),
                        category = category
                    )

                    _uiState.value = CaptureUiState.Success(discoveryId)
                }.onFailure { error ->
                    _uiState.value = CaptureUiState.Error(
                        message = error.localizedMessage ?: "Identification error",
                        capturedImage = bitmap
                    )
                }
            } catch (e: Exception) {
                _uiState.value = CaptureUiState.Error(
                    message = e.localizedMessage ?: "Unknown error",
                    capturedImage = bitmap
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = CaptureUiState.Idle
    }

    fun cancelProcessing() {
        _uiState.value = CaptureUiState.Cancelled
    }
}

sealed class CaptureUiState {
    object Idle : CaptureUiState()

    data class Processing(
        val message: String,
        val capturedImage: Bitmap,
        val progress: Float = 0.5f
    ) : CaptureUiState()

    data class Success(val discoveryId: Long) : CaptureUiState()

    data class Error(
        val message: String,
        val capturedImage: Bitmap? = null
    ) : CaptureUiState()

    object Cancelled : CaptureUiState()
}
