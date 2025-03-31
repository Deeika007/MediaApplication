package com.example.mediaapplication

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MediaPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            MediaPreviewScreen(
                mediaUri = intent.getStringExtra("media_uri") ?: "",
                mediaType = intent.getStringExtra("media_type") ?: "image",
                fileName = intent.getStringExtra("file_name") ?: "Unknown",
                fileSize = intent.getStringExtra("file_size") ?: "Unknown",
                fileDate = intent.getStringExtra("file_date") ?: "Unknown",this
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPreviewScreen(
    mediaUri: String,
    mediaType: String,
    fileName: String,
    fileSize: String,
    fileDate: String,
    context: Context
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MediaPreviewScreen") },
                actions = {
                    TextButton(onClick = { downloadMedia(context, mediaUri, mediaType, fileName) }) {
                        Text(text = "Download", color = Color.Blue)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (mediaType == "image") {
                AsyncImage(
                    model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(mediaUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Preview Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Fit
                )
            } else if (mediaType == "video") {
                VideoPlayer(videoUri = mediaUri)
            }

            Spacer(modifier = Modifier.height(16.dp))

          /*  Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "File Name: $fileName", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Size: $fileSize bytes", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Date: $fileDate", style = MaterialTheme.typography.bodyLarge)
            }*/
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start // Align text to the left
            ) {
                Text(
                    text = "File Name: ",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    fontSize = 18.sp
                )
                Text(text = fileName, style = MaterialTheme.typography.bodyLarge, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Size: ",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    fontSize = 18.sp
                )
                Text(text = "$fileSize bytes", style = MaterialTheme.typography.bodyLarge, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Date: ",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    fontSize = 18.sp
                )
                Text(text = fileDate, style = MaterialTheme.typography.bodyLarge, fontSize = 16.sp)
            }

        }
    }
}



fun downloadMedia(context: Context, mediaUri: String, mediaType: String, fileName: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = URL(mediaUri)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.doInput = true
            connection.connect()

            val inputStream: InputStream = connection.inputStream

            val mimeType = if (mediaType == "image") "image/jpeg" else "video/mp4"
            val folderName = if (mediaType == "image") Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_MOVIES

            // Set up file details
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val newFileName = if (fileName.isNotEmpty()) fileName else "Media_$timeStamp"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$folderName/MyDownloads")
            }

            val contentUri = if (mediaType == "image") {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val resolver = context.contentResolver
            val outputUri = resolver.insert(contentUri, contentValues)

            outputUri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                }
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Downloaded to $folderName/MyDownloads", Toast.LENGTH_LONG).show()
                }
            } ?: throw Exception("Failed to create file in MediaStore")
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                Log.e("TAG", "downloadMedia: failed", e)
                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}




@Composable
fun VideoPlayer(videoUri: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(androidx.media3.common.MediaItem.fromUri(Uri.parse(videoUri)))
            prepare()
            playWhenReady = true
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
     //   mediaViewModel.checkMediaSyncStatus(userId ?: "")
                player = exoPlayer
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}


