package com.example.stressease.History

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.R

data class ChatHistoryItem(
    val userMessage: String,
    val botReply: String,
    val timestamp: Long
)
class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val chatList = mutableListOf<ChatHistoryItem>()

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userText: TextView = itemView.findViewById(R.id.tvUserMessage)
        val botText: TextView = itemView.findViewById(R.id.tvBotReply)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_history, parent, false)
        return HistoryViewHolder(view)
    }
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val chat = chatList[position]
        holder.userText.text = "You: ${chat.userMessage}"
        holder.botText.text = "Bot: ${chat.botReply}"
    }

    override fun getItemCount(): Int = chatList.size

    fun setData(newData: List<ChatHistoryItem>) {
        chatList.clear()
        chatList.addAll(newData)
        notifyDataSetChanged()
    }
}
