package com.example.mediaapplication

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Dao
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: Media)

    @Query("SELECT * FROM media_table")
    suspend fun getAllMedia(): List<Media>

    @Query("SELECT * FROM media_table WHERE userId = :userId")
    fun getMediaByUser(userId: String): List<Media>

    @Query("DELETE FROM media_table WHERE id = :id")
    suspend fun deleteMedia(id: Int)

    @Query("SELECT * FROM media_table")
    fun getAllMediaFlow(): Flow<List<Media>> // ✅ Live updates

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mediaList: List<Media>)
}