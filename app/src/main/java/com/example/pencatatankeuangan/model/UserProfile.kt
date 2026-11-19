package com.example.pencatatankeuangan.model

data class UserProfile(
    val fullName: String = "Kania Khalifa",
    val email: String = "kania@gmail.com",
    val photoUri: String? = null,
    val notificationEnabled: Boolean = true
)
