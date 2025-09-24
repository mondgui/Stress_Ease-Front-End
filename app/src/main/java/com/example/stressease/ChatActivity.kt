package com.example.stressease

import android.R.id.message
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        chatAdapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = chatAdapter

        btnSend.setOnClickListener {
            val userMessage = etMessage.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addMessage(ChatMessage(userMessage, isUser = true))
                etMessage.text.clear()
                sendMessage(userMessage)
            }
        }
    }

    private fun sendMessage(userMessage: String) {
            val request = mapOf("message" to userMessage)
            RetrofitClient.api.sendChat(request)
                .enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(
                        call: Call<Map<String, String>>,
                        response: Response<Map<String, String>>
                    ) {
                        val reply = response.body()?.get("reply") ?: "No response from Flask"
                        addMessage(ChatMessage(reply, isUser = false))
                    }

                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                        addMessage(ChatMessage("Flask Error: ${t.message}", isUser = false))
                    }
                })

        }
        private fun addMessage(chatMessage: ChatMessage) {
        // Add the new message to your list
        messages.add(chatMessage)

        // Notify adapter that a new item is inserted
        chatAdapter.notifyItemInserted(messages.size - 1)

        // Scroll to the latest message
        recyclerView.scrollToPosition(messages.size - 1)
    }

}

