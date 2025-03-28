package com.example.mediaapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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



/*
class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository(application)
  */
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
}*//*



    private val mediaDao = MediaDatabase.getDatabase(application).mediaDao()

    private val _mediaList = MutableStateFlow<List<Media>>(emptyList())
    val mediaList: StateFlow<List<Media>> = _mediaList.asStateFlow()

    init {
        getAllMedia() // Load existing media initially
    }

  */
/*  fun insertMedia(uri: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            mediaDao.insertMedia(Media(uri = uri, type = type))
            _mediaList.value = mediaDao.getAllMedia() // Fetch updated data
        }
    }*//*


    fun insertMedia(uri: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            mediaDao.insertMedia(Media(uri = uri, type = type))
            getAllMedia() // Refresh the list after insertion
        }
    }


 */
/*   fun getAllMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            _mediaList.value = mediaDao.getAllMedia()
        }
    }*//*




    fun getAllMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            _mediaList.value = mediaDao.getAllMedia()
        }
    }


    fun deleteMedia(id: Int) {
        viewModelScope.launch {
            repository.deleteMedia(id)
        }
    }
}*/
class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaDao = MediaDatabase.getDatabase(application).mediaDao()

    private val _mediaList = MutableStateFlow<List<Media>>(emptyList())
    val mediaList: StateFlow<List<Media>> = _mediaList.asStateFlow()

    init {
        getAllMedia() // Load existing media initially
    }

    fun insertMedia(uri: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            mediaDao.insertMedia(Media(uri = uri, type = type))
            refreshMediaList() // Refresh UI safely
        }
    }

    fun getAllMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            val mediaItems = mediaDao.getAllMedia()
            withContext(Dispatchers.Main) {
                _mediaList.value = mediaItems
            }
        }
    }

    fun deleteMedia(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            mediaDao.deleteMedia(id)
            refreshMediaList()
        }
    }

    private suspend fun refreshMediaList() {
        val mediaItems = mediaDao.getAllMedia()
        withContext(Dispatchers.Main) {
            _mediaList.value = mediaItems
        }
    }
}
