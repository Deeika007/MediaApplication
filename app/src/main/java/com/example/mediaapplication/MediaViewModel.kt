package com.example.mediaapplication

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MediaViewModel(application: Application,private val userId: String) : AndroidViewModel(application) {

    private val mediaDao = MediaDatabase.getDatabase(application).mediaDao()
    private val db = FirebaseFirestore.getInstance()
    private val repository = Repository(application)



    private val _mediaList = MutableStateFlow<List<Media>>(emptyList())
    val mediaList: StateFlow<List<Media>> = _mediaList.asStateFlow()

    init {
        // getAllMedia() // Load existing media initially
        refreshMediaList(userId)
    }


    fun insertMedia(uri: String, type: String, userId: String, name: String, size: String, created: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val media = Media(uri = uri, type = type, userId = userId, name = name, size = size, created = created)
            mediaDao.insertMedia(media)

            val insertedData = mediaDao.getMediaByUser(userId)
            Log.d("DB_CHECK", "Inserted Media: $insertedData")

            withContext(Dispatchers.Main) {
                refreshMediaList(userId)
            }
        }
    }


    fun getAllMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            val mediaItems = mediaDao.getAllMedia()
            withContext(Dispatchers.Main) {
                _mediaList.value = mediaItems
            }
        }

        /*  val localMedia = mediaDao.getAllMedia()
            if (localMedia.isEmpty()) {
                getMediaFromFirestore(userId) // Fetch from Firestore if local is empty
            } else {
                withContext(Dispatchers.Main) {
                    _mediaList.value = localMedia // Show local data first
                }*/


        // }
    }


    fun deleteMedia(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            mediaDao.deleteMedia(id)
            // refreshMediaList()
        }
    }

    fun refreshMediaList(userId: String) {

        viewModelScope.launch(Dispatchers.IO) {
            val mediaItems = mediaDao.getMediaByUser(userId)
            withContext(Dispatchers.Main) {
                _mediaList.value = mediaItems
            }
        }
    }


  /*  fun refreshMediaList(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val localMedia = mediaDao.getMediaByUser(userId)

            if (localMedia.isEmpty()) {
                getMediaFromFirestore(userId) // First-time fetch
            } else {
                withContext(Dispatchers.Main) {
                    _mediaList.value = localMedia
                }
            }
        }
    }*/




    fun getMediaFromFirestore(userId: String) {
            db.collection("signup").document(userId).collection("media")
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.d("TAG", "No media found for userId: $userId")
                        _mediaList.value = emptyList()
                    } else {
                        val mediaItems = documents.map { doc ->
                            Media(
                                uri = doc.getString("url") ?: "",
                                type = doc.getString("type") ?: "",
                                userId = userId,
                                name = doc.getString("name") ?: "Unknown",
                                size = doc.getString("size") ?: "",
                                created = doc.getString("Created") ?: "Unknown"

                            )


                        }

                    /*    viewModelScope.launch(Dispatchers.IO) {
                            mediaDao.insertAll(mediaItems) // Store in local database
                            withContext(Dispatchers.Main) {
                                _mediaList.value = mediaItems // Update UI
                            }
                        }*/


                        _mediaList.value = mediaItems


                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("TAG", "Error fetching media: ${exception.message}")
                }
        }







}



