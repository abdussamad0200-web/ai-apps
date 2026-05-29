package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Constrained to a single row
    val name: String = "Abdus Samad",
    val bio: String = "AI Explorer & Creator",
    val status: String = "All-In-One AI Dashboard Active",
    val avatarIdentifier: String = "avatar_neon_1", // Default visual avatar identifier
    val lastUpdated: Long = System.currentTimeMillis()
)
