package com.example.plantdiscoveryjournal.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.plantdiscoveryjournal.data.local.dao.DiscoveryDao
import com.example.plantdiscoveryjournal.data.local.entity.DiscoveryEntity
import com.example.plantdiscoveryjournal.data.remote.api.GeminiApiClient
import com.example.plantdiscoveryjournal.data.remote.api.PlantIdentificationResult
import com.example.plantdiscoveryjournal.domain.model.Discovery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Repository pour gérer les découvertes et l'IA
 */
class DiscoveryRepository(
    private val discoveryDao: DiscoveryDao,
    private val context: Context
) {

    // Convertir Entity en Domain Model
    private fun DiscoveryEntity.toDomain(): Discovery {
        return Discovery(
            id = this.id,
            userId = this.userId,
            name = this.name,
            aiFact = this.aiFact,
            imageLocalPath = this.imageLocalPath,
            timestamp = this.timestamp
        )
    }

    // Obtenir toutes les découvertes d'un utilisateur
    fun getAllDiscoveriesByUser(userId: String): Flow<List<Discovery>> {
        return discoveryDao.getAllDiscoveriesByUser(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    // Obtenir une découverte par ID
    suspend fun getDiscoveryById(id: Long): Discovery? {
        return discoveryDao.getDiscoveryById(id)?.toDomain()
    }

    // Supprimer une découverte
    suspend fun deleteDiscovery(id: Long) {
        discoveryDao.deleteDiscoveryById(id)
    }

    // Sauvegarder une image localement
    suspend fun saveImageLocally(bitmap: Bitmap, userId: String): String = withContext(Dispatchers.IO) {
        val fileName = "discovery_${System.currentTimeMillis()}.jpg"
        val directory = File(context.filesDir, "discoveries/$userId")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        file.absolutePath
    }

    // Identifier une plante avec l'IA Google Gemini
    suspend fun identifyPlant(imagePath: String): Result<PlantIdentificationResult> = withContext(Dispatchers.IO) {
        try {
            // Charger l'image
            val bitmap = BitmapFactory.decodeFile(imagePath)

            // Appeler l'API Gemini
            GeminiApiClient.identifyPlant(bitmap)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sauvegarder une nouvelle découverte
    suspend fun saveDiscovery(
        userId: String,
        name: String,
        fact: String,
        imagePath: String,
        timestamp: Long
    ): Long {
        val entity = DiscoveryEntity(
            userId = userId,
            name = name,
            aiFact = fact,
            imageLocalPath = imagePath,
            timestamp = timestamp
        )
        return discoveryDao.insertDiscovery(entity)
    }
}