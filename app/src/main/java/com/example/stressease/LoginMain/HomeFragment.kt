package com.example.stressease.LoginMain

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.stressease.Chats.ChatFragment
import com.example.stressease.History.History
import com.example.stressease.MoodFragment
import com.google.firebase.auth.FirebaseAuth
import com.example.stressease.R

class HomeFragment: Fragment() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvSubtext: TextView
    private lateinit var btnMood: Button
    private lateinit var btnChat: Button
    private lateinit var btnHistory: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvWelcome = view.findViewById(R.id.tvWelcome)
        tvSubtext = view.findViewById(R.id.tvSubtext)
        btnMood = view.findViewById(R.id.btnMood)
        btnChat = view.findViewById(R.id.btnChat)
        btnHistory = view.findViewById(R.id.btnHistory)

        // Fetch logged-in user
        val user = FirebaseAuth.getInstance().currentUser
        val userName = user?.displayName ?: "User"

        // Welcome message
        tvWelcome.text = "Welcome, $userName 👋"
        tvSubtext.text = "How are you feeling today?"

        // Navigate to Mood fragment
        btnMood.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MoodFragment())
                .addToBackStack(null)
                .commit()
        }

        // Navigate to Chat fragment
        btnChat.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatFragment())
                .addToBackStack(null)
                .commit()
        }

        // Navigate to History activity
        btnHistory.setOnClickListener {
            startActivity(Intent(requireContext(), History::class.java))
        }
    }
}
