package com.example.questrelife.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Data classes for requests and responses
data class ImageRequest(
    val prompt: String
)

data class ImageResponse(
    val image_url: String?,
    val image_base64: String?
)

// Retrofit interface
interface ImageApi {
    @Headers("Content-Type: application/json")
    @POST("generate") // <-- adjust to your endpoint
    suspend fun generateImage(@Body request: ImageRequest): ImageResponse
}

// Singleton client builder
object ApiClient {
    fun create(baseUrl: String, apiKey: String): ImageApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ImageApi::class.java)
    }
}
