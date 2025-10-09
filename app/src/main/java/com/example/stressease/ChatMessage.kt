package com.example.stressease

import android.icu.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    // The actual content of the message
    val message: String,// True if the message is from the user, false if from the bot
    val isUser: Boolean,

    // The emotion associated with the message, with a default value
    val emotion: String = "neutral",

    // The time the message was created
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)



//package com.example.stressease
//
//import android.icu.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//data class ChatMessage(
//    val text: String,
//    val isUser: Boolean,
//    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
//)
