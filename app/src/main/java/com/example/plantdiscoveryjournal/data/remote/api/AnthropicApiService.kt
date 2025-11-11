package com.example.plantdiscoveryjournal.data.remote.api

import com.example.plantdiscoveryjournal.data.remote.model.AnthropicRequest
import com.example.plantdiscoveryjournal.data.remote.model.AnthropicResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Interface Retrofit pour l'API Anthropic
 */
interface AnthropicApiService {

    @POST("v1/messages")
    @Headers(
        "anthropic-version: 2023-06-01",
        "Content-Type: application/json"
    )
    suspend fun createMessage(
        @Body request: AnthropicRequest
    ): Response<AnthropicResponse>
}