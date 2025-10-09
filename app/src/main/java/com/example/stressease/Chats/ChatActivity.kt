package com.example.stressease.Chats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.Api.ChatRequest
import com.example.stressease.Api.RetrofitClient
import com.example.stressease.History.History
import com.example.stressease.LocalStorageOffline.SharedPreference
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
    private var currentSessionId: String? = null

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
                // The user's message is added immediately for a responsive feel.
                // The API response will add the bot's reply later.
                addMessage(chat)
                saveChatMessage(chat)
                etMessage.text.clear()
                sendMessage(userMessage)
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
                        val chatResponse = response.body()!!

                        // FINAL FIX: Add the safe call operator (?.) before accessing 'content' and 'role'
                        val botReply = chatResponse.ai_response?.content ?: "No reply from AI."
                        val botRole = chatResponse.ai_response?.role ?: "assistant"

                        // Add the bot's reply to the chat
                        addMessage(
                            ChatMessage(
                                botReply,
                                isUser = false,
                                emotion = botRole,
                                message = botReply
                            )
                        )

                        // Save the new session_id from the 'chatResponse' variable
                        chatResponse.session_id?.let { newSessionId ->
                            currentSessionId = newSessionId
                        }
                    } else {
                        addMessage(
                            ChatMessage(
                                "Error: ${response.code()}",
                                isUser = false,
                                emotion = "neutral",
                                message = "Server returned an error."
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addMessage(
                        ChatMessage(
                            "Failed: ${e.message}",
                            isUser = false,
                            emotion = "neutral",
                            message = "Could not connect to the server."
                        )
                    )
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
