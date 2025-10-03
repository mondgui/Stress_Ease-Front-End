package com.example.stressease // Make sure this package name is correct

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.text
import com.example.stressease.databinding.ActivityLoginBinding // This will be auto-generated
import kotlin.text.isEmpty
import kotlin.text.trim

class LoginActivity : AppCompatActivity() {

    // Declare a variable for view binding
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and set the content view
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the click listener for the login button
        binding.loginButton.setOnClickListener {
            handleLogin()
        }

        // Set up the click listener for the "Sign Up" text
        binding.signUpText.setOnClickListener {
            // For now, just show a message. You can navigate to a SignUpActivity later.
            Toast.makeText(this, "Sign Up clicked!", Toast.LENGTH_SHORT).show()
            // Example:
            // val intent = Intent(this, SignUpActivity::class.java)
            // startActivity(intent)
        }
    }

    private fun handleLogin() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        // --- Basic Validation ---
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email cannot be empty"
            return
        } else {
            // Clear error if user corrects it
            binding.emailInputLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password cannot be empty"
            return
        } else {
            // Clear error if user corrects it
            binding.passwordInputLayout.error = null
        }

        // --- Authentication Logic ---
        // TODO: Replace this with your actual authentication logic (e.g., Firebase, your own backend)

        // For demonstration, we'll use a simple hardcoded check
        if (email == "user@example.com" && password == "password123") {
            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

            // Navigate to the main part of the app
            val intent = Intent(this, MainActivity::class.java)
            // Clear the task stack so the user can't go back to the login screen
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show()
        }
    }
}
