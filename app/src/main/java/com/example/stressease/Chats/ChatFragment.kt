package com.example.stressease.Chats

import android.content.Intent
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.LocalStorageOffline.SharedPreference
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stressease.Api.AiResponse
import com.example.stressease.Api.ApiService
import com.example.stressease.Api.ChatRequest
import com.example.stressease.Api.ChatResponse
import com.example.stressease.Api.RetrofitClient
import com.example.stressease.History.History
import com.example.stressease.R
import com.example.stressease.SOS.SOS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var next: Button
    private lateinit var prev: Button
    private lateinit var chatAdapter: ChatAdapter

    private var messages: MutableList<ChatMessage> = mutableListOf()

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        next = findViewById(R.id.btnNext)
        prev = findViewById(R.id.btnBack)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Load old chat history from SharedPreferences
        messages = SharedPreference.loadChatList(this, "chat_history")

        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        chatAdapter = ChatAdapter(messages)
        recyclerView.adapter = chatAdapter

        btnSend.setOnClickListener {
            val userMessage = etMessage.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                val chat = ChatMessage(userMessage, isUser = true, emotion = "neutral", message = userMessage)
                addMessage(chat)
                saveChatMessage(chat)
                etMessage.text.clear()
                sendMessage(userMessage) // now works properly
            }
        }
        next.setOnClickListener {
            startActivity(Intent(this, History::class.java))
            finish()
        }
        prev.setOnClickListener {
            startActivity(Intent(this, SOS::class.java))
            finish()
        }
    }
    private fun addMessage(chatMessage: ChatMessage) {
        messages.add(chatMessage)
        chatAdapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
        SharedPreference.saveChatList(this, "chat_history", messages)
    }
    private var currentSessionId :String?=null

    private fun sendMessage(userMessage: String) {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val token = prefs.getString("authToken", null)

        if (token == null) {
            Toast.makeText(this, "No token found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ChatRequest(userMessage, session_id = currentSessionId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.sendMessage("Bearer $token", request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val chatResp = response.body()!!
                        val botReply = chatResp.ai_response?.content ?: "No reply"

                        // Add user message
                        addMessage(
                            ChatMessage(
                                userMessage,
                                isUser = true,
                                emotion = "neutral",
                                message = userMessage
                            )
                        )

                        // Add bot reply
                        addMessage(
                            ChatMessage(
                                botReply,
                                isUser = false,
                                emotion = chatResp.ai_response?.role ?: "assistant",
                                message = botReply
                            )
                        )
                    } else {
                        Toast.makeText(this@ChatActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChatActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    private fun saveChatMessage(chatMessage: ChatMessage) {
        val userId = auth.currentUser?.uid ?: return
        val chatData = hashMapOf(
            "message" to chatMessage.text,
            "sender" to if (chatMessage.isUser) "user" else "bot",
            "emotion" to chatMessage.emotion,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users")
            .document(userId)
            .collection("chats")
            .add(chatData)
    }
}