package com.example.mediaapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/*class MediaViewModel(application: Application) : AndroidViewModel(application) {


    private val repository = Repository(application)

    fun insertMedia(uri: String, type: String) {
        viewModelScope.launch {
            repository.insertMedia(Media(uri = uri, type = type))
        }
    }

    fun getAllMedia(onResult: (List<Media>) -> Unit) {
        viewModelScope.launch {
            val mediaList = repository.getAllMedia()
            onResult(mediaList)
        }
    }

    fun deleteMedia(id: Int) {
        viewModelScope.launch {
            repository.deleteMedia(id)
        }
    }
}*/



class MediaViewModel(application: Application) : AndroidViewModel(application) {


  /*  private val repository = Repository(application)

    fun insertMedia(uri: String, type: String) {
        viewModelScope.launch {
            repository.insertMedia(Media(uri = uri, type = type))
        }
    }

    fun getAllMedia(onResult: (List<Media>) -> Unit) {
        viewModelScope.launch {
            val mediaList = repository.getAllMedia()
            onResult(mediaList)
        }
    }

    fun deleteMedia(id: Int) {
        viewModelScope.launch {
            repository.deleteMedia(id)
        }
    }
}*/


    private val mediaDao = MediaDatabase.getDatabase(application).mediaDao()

    private val _mediaList = MutableStateFlow<List<Media>>(emptyList())
    val mediaList: StateFlow<List<Media>> = _mediaList

    fun insertMedia(uri: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            mediaDao.insertMedia(Media(uri = uri, type = type))
            _mediaList.value = mediaDao.getAllMedia() // Fetch updated data
        }
    }

    fun getAllMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            _mediaList.value = mediaDao.getAllMedia()
        }
    }
}