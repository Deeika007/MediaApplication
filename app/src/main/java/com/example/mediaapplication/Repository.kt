package com.example.mediaapplication

import android.content.Context

class Repository(context: Context) {

private val mediaDao = MediaDatabase.getDatabase(context).mediaDao()

suspend fun insertMedia(media: Media) {
    mediaDao.insertMedia(media)
}

suspend fun getAllMedia(): List<Media> {
    return mediaDao.getAllMedia()
}

suspend fun deleteMedia(id: Int) {
    mediaDao.deleteMedia(id)
}
}
