package com.example.plantdiscoveryjournal.data.remote.api

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.example.plantdiscoveryjournal.BuildConfig

/**
 * Client pour l'API Google Gemini
 */

object GeminiApiClient {

    private val model: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    /**
     * Identifie une plante à partir d'une image
     */
    suspend fun identifyPlant(bitmap: Bitmap): Result<PlantIdentificationResult> {
        return try {
            val prompt = """
                Analysez cette image et identifiez la plante, fleur ou insecte présent.
                
                Répondez STRICTEMENT au format suivant (sans texte supplémentaire):
                NOM: [nom précis en français]
                FAIT: [Deux phrases courtes et intéressantes sur cet objet.]
                
                Si vous ne pouvez pas identifier l'objet, utilisez "Objet non identifiable" comme nom.
            """.trimIndent()

            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = model.generateContent(inputContent)
            val responseText = response.text ?: throw Exception("Aucune réponse de Gemini")

            val result = parseResponse(responseText)
            Result.success(result)

        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("API key not valid") == true ||
                        e.message?.contains("API_KEY_INVALID") == true ->
                    "Clé API invalide. Vérifiez votre clé sur https://aistudio.google.com/app/apikey"

                e.message?.contains("not found") == true ||
                        e.message?.contains("404") == true ->
                    "Modèle non trouvé. Vérifiez que vous avez accès à l'API Gemini."

                e.message?.contains("quota") == true ||
                        e.message?.contains("429") == true ->
                    "Quota dépassé. Attendez quelques minutes avant de réessayer."

                e.message?.contains("PERMISSION_DENIED") == true ->
                    "Permission refusée. Activez l'API Generative AI sur Google Cloud Console."

                else ->
                    "Erreur Gemini: ${e.message ?: e.localizedMessage}"
            }

            Result.failure(Exception(errorMessage))
        }
    }

    /**
     * Parse la réponse de Gemini
     */
    private fun parseResponse(text: String): PlantIdentificationResult {
        var name = "Plante Inconnue"
        var fact = "Une découverte intéressante à identifier!"

        try {
            // Nettoyer le texte
            val cleanText = text.trim()

            // Extraire le nom (première ligne après "NOM:")
            val nomPattern = Regex("""NOM:\s*(.+?)(?=\n|FAIT:|$)""", RegexOption.IGNORE_CASE)
            nomPattern.find(cleanText)?.let {
                name = it.groupValues[1].trim()
            }

            // Extraire le fait (tout après "FAIT:")
            val faitPattern = Regex("""FAIT:\s*(.+)""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            faitPattern.find(cleanText)?.let {
                fact = it.groupValues[1].trim()
            }
        } catch (e: Exception) {
            // En cas d'erreur de parsing, utiliser le texte brut
            if (text.isNotBlank()) {
                name = "Identification réussie"
                fact = text.take(200) // Limiter à 200 caractères
            }
        }

        return PlantIdentificationResult(name, fact)
    }
}

/**
 * Résultat d'identification
 */
data class PlantIdentificationResult(
    val name: String,
    val fact: String
)