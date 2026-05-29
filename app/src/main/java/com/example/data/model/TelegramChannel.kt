package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telegram_channels")
data class TelegramChannel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val identifier: String, // e.g. @your_channel or -100123456789
    val botToken: String, // Secret bot token
    val addedAt: Long = System.currentTimeMillis()
)
