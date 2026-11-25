package com.example.pencatatankeuangan.model

data class UserProfile(
    val fullName: String = "",
    val email: String = "",
    val photoUri: String? = null,
    val notificationEnabled: Boolean = true
)
