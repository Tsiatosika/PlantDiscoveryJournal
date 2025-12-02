package com.example.plantdiscoveryjournal.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modèle de domaine pour une découverte
 */
data class Discovery(
    val id: Long,
    val userId: String,
    val name: String,
    val aiFact: String,
    val imageLocalPath: String,
    val timestamp: Long,
    val location: String = "",
    val notes: String = "",
    val category: String = "Plante"
) {
    fun getFormattedDate(): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRENCH)
        return format.format(date)
    }
}