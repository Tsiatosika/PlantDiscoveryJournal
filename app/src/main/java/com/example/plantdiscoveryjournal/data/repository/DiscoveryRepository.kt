package com.example.plantdiscoveryjournal.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.plantdiscoveryjournal.data.local.dao.DiscoveryDao
import com.example.plantdiscoveryjournal.data.local.entity.DiscoveryEntity
import com.example.plantdiscoveryjournal.data.remote.api.AnthropicApiClient
import com.example.plantdiscoveryjournal.data.remote.model.*
import com.example.plantdiscoveryjournal.domain.model.Discovery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
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

    // Identifier une plante avec l'IA Anthropic
    suspend fun identifyPlant(imagePath: String): Result<PlantIdentificationResult> = withContext(Dispatchers.IO) {
        try {
            // Charger l'image et la convertir en base64
            val bitmap = BitmapFactory.decodeFile(imagePath)
            val base64Image = bitmapToBase64(bitmap)

            // Créer la requête pour Claude
            val request = AnthropicRequest(
                messages = listOf(
                    Message(
                        role = "user",
                        content = listOf(
                            ContentBlock.ImageBlock(
                                source = ImageSource(
                                    mediaType = "image/jpeg",
                                    data = base64Image
                                )
                            ),
                            ContentBlock.TextBlock(
                                text = """Identifiez cet objet (plante, fleur ou insecte) et écrivez un fait amusant de deux phrases à son sujet.
                                
Répondez UNIQUEMENT au format suivant (sans aucune autre texte) :
NOM: [nom de l'objet]
FAIT: [fait amusant en deux phrases]"""
                            )
                        )
                    )
                )
            )

            // Appeler l'API
            val response = AnthropicApiClient.apiService.createMessage(request)

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                val textContent = apiResponse.content.firstOrNull { it.type == "text" }?.text

                if (textContent != null) {
                    val result = parseAiResponse(textContent)
                    Result.success(result)
                } else {
                    Result.failure(Exception("Pas de contenu texte dans la réponse"))
                }
            } else {
                Result.failure(Exception("Erreur API: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Convertir bitmap en base64
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    // Parser la réponse de l'IA
    private fun parseAiResponse(text: String): PlantIdentificationResult {
        val lines = text.trim().lines()
        var name = "Plante Inconnue"
        var fact = "Une découverte intéressante!"

        for (line in lines) {
            when {
                line.startsWith("NOM:", ignoreCase = true) -> {
                    name = line.substring(4).trim()
                }
                line.startsWith("FAIT:", ignoreCase = true) -> {
                    fact = line.substring(5).trim()
                }
            }
        }

        return PlantIdentificationResult(name, fact)
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