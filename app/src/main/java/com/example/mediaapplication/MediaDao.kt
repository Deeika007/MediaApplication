package com.example.mediaapplication

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Dao

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: Media)

    @Query("SELECT * FROM media_table")
    suspend fun getAllMedia(): List<Media>

    @Query("DELETE FROM media_table WHERE id = :id")
    suspend fun deleteMedia(id: Int)
}