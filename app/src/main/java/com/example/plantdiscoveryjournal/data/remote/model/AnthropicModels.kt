package com.example.plantdiscoveryjournal.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Modèles pour l'API Anthropic Claude
 */

// Requête
data class AnthropicRequest(
    val model: String = "claude-sonnet-4-20250514",
    @SerializedName("max_tokens")
    val maxTokens: Int = 1024,
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: List<ContentBlock>
)

sealed class ContentBlock {
    data class TextBlock(
        val type: String = "text",
        val text: String
    ) : ContentBlock()

    data class ImageBlock(
        val type: String = "image",
        val source: ImageSource
    ) : ContentBlock()
}

data class ImageSource(
    val type: String = "base64",
    @SerializedName("media_type")
    val mediaType: String,
    val data: String
)

// Réponse
data class AnthropicResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<ResponseContent>,
    val model: String,
    @SerializedName("stop_reason")
    val stopReason: String?,
    val usage: Usage
)

data class ResponseContent(
    val type: String,
    val text: String?
)

data class Usage(
    @SerializedName("input_tokens")
    val inputTokens: Int,
    @SerializedName("output_tokens")
    val outputTokens: Int
)

// Résultat d'identification
data class PlantIdentificationResult(
    val name: String,
    val fact: String
)