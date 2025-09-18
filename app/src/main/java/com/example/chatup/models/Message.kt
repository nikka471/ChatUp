package com.example.chatup.models

import com.google.firebase.Timestamp

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)