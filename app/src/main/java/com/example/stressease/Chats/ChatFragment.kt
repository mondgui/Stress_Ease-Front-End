package com.example.stressease.Chats

import android.content.Intent
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.LocalStorageOffline.SharedPreference
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stressease.Api.ChatRequest
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
import android.widget.TextView
import com.google.firebase.firestore.Query

class ChatFragment : Fragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvChat)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)
        next = view.findViewById(R.id.btnNext)
        prev = view.findViewById(R.id.btnBack)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val isNewSession = arguments?.getBoolean("isNewSession") ?: false

        if (isNewSession) {

            messages.clear()
            SharedPreference.saveChatList(requireContext(), "chat_history", messages)
        } else {
            // Load previous chat
            messages = SharedPreference.loadChatList(requireContext(), "chat_history")
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }

        // Load old chat history
        messages = SharedPreference.loadChatList(requireContext(), "chat_history")

        recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }

        chatAdapter = ChatAdapter(messages)
        recyclerView.adapter = chatAdapter

        // Send message
        btnSend.setOnClickListener {
            val userMessage = etMessage.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                val chat = ChatMessage(
                    userMessage,
                    isUser = true,
                    emotion = "neutral",
                    message = userMessage
                )
                addMessage(chat)
                saveChatMessage(chat)
                etMessage.text.clear()
                sendMessage(userMessage)
            }
        }

        next.setOnClickListener {
            startActivity(Intent(requireContext(), History::class.java))
        }
        prev.setOnClickListener {
            startActivity(Intent(requireContext(), SOS::class.java))
        }
    }

    private fun addMessage(chatMessage: ChatMessage) {
        if (!::chatAdapter.isInitialized) return
        messages.add(chatMessage)
        chatAdapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
        SharedPreference.saveChatList(requireContext(), "chat_history", messages)
    }


    private fun sendMessage(userMessage: String) {
        val prefs = requireContext().getSharedPreferences("AppPrefs", AppCompatActivity.MODE_PRIVATE)
        val token = prefs.getString("authToken", null)

        if (token == null) {
            Toast.makeText(requireContext(), "No token found. Please log in again.", Toast.LENGTH_SHORT).show()
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
                        val botMessage = ChatMessage(
                            text = botReply,
                            isUser = false,
                            emotion = chatResp.ai_response?.role ?: "assistant",
                            message = botReply
                        )
                        addMessage(botMessage)
                        saveChatMessage(botMessage)
                    } else {
                        generateReplyFromFirestoreContext(userMessage)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ChatFragment", "Server error: ${e.message}")
                    generateReplyFromFirestoreContext(userMessage)
                }
            }
        }
    }


    private fun generateReplyFromFirestoreContext(userMessage: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("chats")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(25)
            .get()
            .addOnSuccessListener { docs ->
                val pairs = mutableListOf<Pair<String, String>>()
                var lastUserMsg: String? = null

                for (doc in docs) {
                    val sender = doc.getString("sender") ?: ""
                    val message = doc.getString("message") ?: ""
                    if (sender == "user") lastUserMsg = message
                    else if (sender == "bot" && lastUserMsg != null) {
                        pairs.add(Pair(lastUserMsg, message))
                        lastUserMsg = null
                    }
                }

                // Dynamic matching logic
                val matched = pairs.find { (u, _) ->
                    userMessage.contains(u.split(" ").firstOrNull() ?: "", ignoreCase = true)
                }

                val offlineReply = matched?.second
                    ?: "I recall we talked about this before — tell me more."

                val botMessage = ChatMessage(
                    text = offlineReply,
                    isUser = false,
                    emotion = "assistant",
                    message = offlineReply
                )
                addMessage(botMessage)
                saveChatMessage(botMessage)
            }
            .addOnFailureListener {
                val fallback = "I'm here for you, even without the server 😊"
                val botMessage = ChatMessage(
                    text = fallback,
                    isUser = false,
                    emotion = "assistant",
                    message = fallback
                )
                addMessage(botMessage)
                saveChatMessage(botMessage)
            }
    }


    private fun saveChatMessage(chatMessage: ChatMessage) {
        val userId = auth.currentUser?.uid ?: return
        val chatData = hashMapOf(
            "message" to chatMessage.message.ifEmpty { chatMessage.text },
            "sender" to if (chatMessage.isUser) "user" else "bot",
            "emotion" to chatMessage.emotion,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .collection("chats")
            .add(chatData)
            .addOnSuccessListener {
                Log.d("Firestore", "✅ Saved: ${chatData["sender"]} → ${chatData["message"]}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "❌ Failed to save chat", e)
            }
    }

    private fun archivePreviousChat() {
        if (messages.isEmpty()) return

        val userId = auth.currentUser?.uid ?: return
        val timestamp = System.currentTimeMillis()

        val historyCollection = db.collection("users")
            .document(userId)
            .collection("chatHistory")

        for (msg in messages) {
            val chatData = hashMapOf(
                "userMessage" to if (msg.isUser) msg.text else "",
                "botReply" to if (!msg.isUser) msg.text else "",
                "timestamp" to timestamp
            )
            historyCollection.add(chatData)
        }

        messages.clear()
        SharedPreference.saveChatList(requireContext(), "chat_history", messages)
    }
    fun refreshChat() {

        messages.clear()


        messages.addAll(SharedPreference.loadChatList(requireContext(), "chat_history"))


        chatAdapter.notifyDataSetChanged()


        recyclerView.scrollToPosition(messages.size - 1)


        Log.d("ChatFragment", "ChatFragment reloaded at ${System.currentTimeMillis()}")
    }

}
