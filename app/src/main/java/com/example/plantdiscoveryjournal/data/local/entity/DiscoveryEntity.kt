package com.example.plantdiscoveryjournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant une découverte de plante/insecte
 */
@Entity(tableName = "discoveries")
data class DiscoveryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: String,          // ID de l'utilisateur authentifié
    val name: String,            // Nom formel identifié par l'IA
    val aiFact: String,          // Fait amusant généré par l'IA
    val imageLocalPath: String,  // Chemin vers l'image stockée localement
    val timestamp: Long,         // Horodatage en milliseconds

    val createdAt: Long = System.currentTimeMillis(),

    val category: String = "Plante"
)
