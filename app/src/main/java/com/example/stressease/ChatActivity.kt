package com.example.stressease

import android.content.Intent
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.ChatAdapter
import com.example.stressease.ChatMessage
import com.example.stressease.RetrofitClient
import com.example.stressease.SharedPreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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


        messages = SharedPreference.loadChatList(this, "chat_history")

        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        chatAdapter = ChatAdapter(messages)
        recyclerView.adapter = chatAdapter


        btnSend.setOnClickListener {
            val userMessage = etMessage.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addMessage(ChatMessage(userMessage, isUser = true, emotion = "neutral",message=userMessage))
                saveChatMessage(ChatMessage(userMessage,isUser = true, emotion = "neutral",message=userMessage))
                Toast.makeText(this, "Message saved", Toast.LENGTH_SHORT).show()
                etMessage.text.clear()
                sendMessage(userMessage)
            }
        }

        next.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
            finish()
        }
        prev.setOnClickListener {
            startActivity(Intent(this, MoodActivity::class.java))
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

        addMessage(ChatMessage("Analyzing...", isUser = false, emotion = "neutral",message=userMessage))

        val request = mapOf("message" to userMessage)

        RetrofitClient.api.sendChat(request).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {

                if (messages.isNotEmpty() && messages.last().text == "Analyzing...") {
                    messages.removeAt(messages.size - 1)
                    chatAdapter.notifyItemRemoved(messages.size)
                }

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val botReply = body["reply"] ?: "No response"
                    val emotion = body["emotion"] ?: "neutral"
                    addMessage(ChatMessage(botReply, isUser = false, emotion = emotion,message=userMessage))
                } else {
                    addMessage(ChatMessage("Error: Could not get response", isUser = false,emotion="Neutral",message=userMessage))
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                if (messages.isNotEmpty() && messages.last().text == "Analyzing...") {
                    messages.removeAt(messages.size - 1)
                    chatAdapter.notifyItemRemoved(messages.size)
                }
                addMessage(ChatMessage("Failed to connect: ${t.message}", isUser = false,emotion="Neutral",message=userMessage))
            }
        })
    }
    private fun saveChatMessage(chatMessage: ChatMessage){
        val userId = auth.currentUser?.uid ?: return
        val chatData = hashMapOf(
            "message" to chatMessage.text,
            "sender" to "user",
            "emotion" to chatMessage.emotion,
            "timestamp" to System.currentTimeMillis()

        )
        db.collection("users")
            .document(userId)
            .collection("chats")
            .add(chatData)
            .addOnSuccessListener {
                Toast.makeText(this, "Chat saved", Toast.LENGTH_SHORT).show()
            }
    }
}
