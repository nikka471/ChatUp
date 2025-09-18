package com.example.chatup.models

data class User(
    val uid: String = "",
    val email: String = "",
    var isActive: Boolean = true,
    var isAdmin: Boolean = false
)