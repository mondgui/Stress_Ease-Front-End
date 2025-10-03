package com.example.stressease

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("chatbot/message") // for real time chat
    fun sendChat(@Body payload: Map<String, String>): Call<Map<String, String>>

    @POST("mood/log") //for sentiment analysis /text extraction and generation
    fun logMood(@Body payload: Map<String, String>): Call<Map<String, String>>
}

