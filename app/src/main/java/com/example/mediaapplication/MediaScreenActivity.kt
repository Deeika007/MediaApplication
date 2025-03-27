package com.example.mediaapplication

import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage


class MediaScreenActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
          //  val media = intent.getParcelableExtra<Media>("media")
            val mediaList: ArrayList<Media>? = intent.getParcelableArrayListExtra("media")
            MediaScreen(mediaList)

        }



    }
}
@Composable
fun MediaScreen(mediaList: ArrayList<Media>?) {
    mediaList?.let { list -> // Correctly handle list iteration
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            list.forEach { media ->  // Iterate through the list
                Text(text = "Media ID: ${media.id}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Type: ${media.type}", fontSize = 18.sp)

                if (media.type == "image") {
                    AsyncImage(
                        model = media.uri,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else if (media.type == "video") {
                    AndroidView(
                        factory = { context ->
                            VideoView(context).apply {
                                setVideoURI(Uri.parse(media.uri))
                                start()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            }
        }
    }
}








