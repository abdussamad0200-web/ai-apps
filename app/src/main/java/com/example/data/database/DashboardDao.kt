package com.example.data.database

import androidx.room.*
import com.example.data.model.HistoryLog
import com.example.data.model.TelegramChannel
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    // --- Telegram Channels ---
    @Query("SELECT * FROM telegram_channels ORDER BY addedAt DESC")
    fun getAllTelegramChannels(): Flow<List<TelegramChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelegramChannel(channel: TelegramChannel)

    @Delete
    suspend fun deleteTelegramChannel(channel: TelegramChannel)

    @Query("DELETE FROM telegram_channels WHERE id = :channelId")
    suspend fun deleteTelegramChannelById(channelId: Int)

    // --- History Logs ---
    @Query("SELECT * FROM history_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAllHistoryLogs(): Flow<List<HistoryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryLog(log: HistoryLog)

    @Query("DELETE FROM history_logs")
    suspend fun clearHistoryLogs()
}
