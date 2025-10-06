package com.example.stressease

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

data class ChatRequest(val message: String,val session_id:String?=null)
data class AiResponse(
    val content: String,
    val timestamp: String,
    val role: String
)

data class ChatResponse(val reply: String, val emotion: String,val ai_response: AiResponse?,val session_id:String?)

data class CrisisContact(
    val id: String?,
    val type: String?,
    val name: String?,
    val number: String?,
    val description: String?,
    val website: String?,
    val availability: String?,
    val country: String?,
    val priority: Int?
)

data class CrisisResponse(val success: Boolean,
                          val message: String?,
                          val contacts: List<CrisisContact>?)

interface ApiService {
    @POST("api/chat/message") // for real time chat
    suspend fun sendMessage(@Header("Authorization") authHeader: String, @Body request:ChatRequest): Response<ChatResponse>

    @POST("mood/log") //for sentiment analysis /text extraction and generation
    fun logMood(@Body payload: Map<String, String>): Call<Map<String, String>>

    @GET("api/crisis-contacts")
    suspend fun getCrisisContacts(
        @Header("Authorization") authHeader: String): Response<CrisisResponse>
}

