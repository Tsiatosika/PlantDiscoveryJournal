package com.example.plantdiscoveryjournal.data.remote.api

import android.R.attr.content
import android.R.id.content
import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

/**
 * Client pour l'API Google Gemini
 */
object GeminiApiClient {

    private const val API_KEY = "AIzaSyA83dgAeYqGPFGTBe2lj207XqzMf-KTnVU"

    // Modèle Gemini Pro Vision pour l'analyse d'images
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = API_KEY
    )

    /**
     * Identifie une plante à partir d'une image
     */
    suspend fun identifyPlant(bitmap: Bitmap): Result<PlantIdentificationResult> {
        return try {
            val prompt = """
                Analysez cette image et identifiez la plante, fleur ou insecte.
                
                Répondez UNIQUEMENT au format suivant (sans aucun autre texte):
                NOM: [nom précis de l'objet en français]
                FAIT: [deux phrases intéressantes et amusantes à son sujet]
                
                Si vous ne pouvez pas identifier clairement l'objet, indiquez "Objet non identifiable" comme nom.
            """.trimIndent()

            val content = content {
                image(bitmap)
                text(prompt)
            }

            val response = model.generateContent(content)
            val responseText = response.text ?: throw Exception("Pas de réponse de Gemini")

            val result = parseResponse(responseText)
            Result.success(result)

        } catch (e: Exception) {
            Result.failure(Exception("Erreur Gemini: ${e.localizedMessage}"))
        }
    }

    /**
     * Parse la réponse de Gemini
     */
    private fun parseResponse(text: String): PlantIdentificationResult {
        val lines = text.trim().lines()
        var name = "Plante Inconnue"
        var fact = "Une découverte intéressante à identifier!"

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
}


 //Résultat d'identification
 data class PlantIdentificationResult(
    val name: String,
    val fact: String
)