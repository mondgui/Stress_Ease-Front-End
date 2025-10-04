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


interface ApiService {
    @POST("api/chat/message") // for real time chat
    suspend fun sendMessage(@Header("Authorization") authHeader: String, @Body request:ChatRequest): Response<ChatResponse>

    @POST("mood/log") //for sentiment analysis /text extraction and generation
    fun logMood(@Body payload: Map<String, String>): Call<Map<String, String>>
}

