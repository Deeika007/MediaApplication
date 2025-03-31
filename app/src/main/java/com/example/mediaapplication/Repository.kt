package com.example.mediaapplication

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage

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


/*
    fun uploadMediaToFirebase(uri: Uri, type: String, userId: String, context: Context) {
        val timestamp = System.currentTimeMillis()
        val storageRef = FirebaseStorage.getInstance().reference.child("signup/$userId/$timestamp")

        storageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveMediaToFirestore(downloadUri.toString(), type, userId)
                    Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()

                    // Mark as uploaded in local database
                    viewModel.markMediaAsUploaded(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
*/






}
