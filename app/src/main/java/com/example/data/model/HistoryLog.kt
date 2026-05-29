package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_logs")
data class HistoryLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val toolName: String, // e.g. "AI Song Maker", "Translator", "Social Gen"
    val promptInput: String,
    val generatedOutput: String,
    val timestamp: Long = System.currentTimeMillis()
)
