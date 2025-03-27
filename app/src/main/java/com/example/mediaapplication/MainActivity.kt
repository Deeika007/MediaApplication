package com.example.mediaapplication

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    /*private val mediaViewModel: MediaViewModel by viewModels(

    )*/
   // private val mediaViewModel: MediaViewModel by viewModels()
    private val mediaViewModel: MediaViewModel by viewModels {
        MediaViewModelFactory(application)
    }
    /*private val mediaViewModel: MediaViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }
*/
    val REQUEST_CODE: Int = 102
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageExample(this,viewModel = mediaViewModel)
        }



    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array< String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            val allGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allGranted) {
                Toast.makeText(this, "Permissions Granted!", Toast.LENGTH_SHORT).show()
                // Now, proceed with picking image after permissions are granted
                pickImage()
            } else {
                Toast.makeText(this, "Permissions Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE)
    }
}


@Composable
fun ImageExample(context: Context,viewModel: MediaViewModel) {


    //  var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

   // var mediaList by remember { mutableStateOf<List<Media>>(emptyList()) }

    val mediaList by viewModel.mediaList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getAllMedia() // Load data when Composable starts
    }

    // ✅ Declare `rememberLauncherForActivityResult` inside the Composable
   /* val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let { imageUri = it }
        }
    }*/


    val pickMediaPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                // Handle selected video URI
                // videoView.setVideoURI(it) // Example: set in VideoView
                // videoView.start()

                selectedUri = it
                val type = if (context.contentResolver.getType(it)?.startsWith("image/") == true) "image" else "video"
                viewModel.insertMedia(it.toString(), type)
               // viewModel.getAllMedia { list -> mediaList = list }


            }
        }
    }
    Log.d("TAG", "ImageExample: mediaList"+mediaList)

    if (mediaList.isNotEmpty()) {
        val intent = Intent(context, MediaScreenActivity::class.java).apply {
            putParcelableArrayListExtra("media", ArrayList(mediaList)) // ✅ Convert to ArrayList
        }
        context.startActivity(intent)
    }

    Column(
        // we are using column to align our
        // imageview to center of the screen.
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),


        // below line is used for specifying
        // vertical arrangement.
        verticalArrangement = Arrangement.Center,

        // below line is used for specifying
        // horizontal arrangement.
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // below line is used for creating a variable
        // for our image resource file.
        val painter = painterResource(id = R.drawable.file
        )

        // below is the composable for image.
        Image(
            // first parameter of our Image
            // is our image path which we have created
            // above
            painter = painter,
            contentDescription = "Sample Image",

            // below line is used for creating a modifier for our image
            // which includes image size, padding and border
            modifier = Modifier
                .height(300.dp)
                .width(300.dp)
                .padding(16.dp)
                .border(2.dp, Color.Black, CircleShape)
                .clickable {

                    if (checkStoragePermission(context as Activity)) {
                        /* val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                         pickImageLauncher.launch(intent)*/


                        /*  val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                        pickVideoLauncher.launch(intent)*/
                        val intent = Intent(Intent.ACTION_PICK).apply {
                            type = "*/*"
                            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                        }
                        pickMediaPicker.launch(intent)

                    } else {
                        checkAndRequestPermissions(context as Activity)
                    }
                },


            // below line is used to give
            // alignment to our image view.
            alignment = Alignment.Center,

            // below line is used to scale our image
            // we are using fit for it.
            contentScale = ContentScale.Fit,

            // below line is used to define the opacity of the image.
            // Here, it is set to the default alpha value, DefaultAlpha.
            alpha = DefaultAlpha,





            )
    }
}





/*
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MediaApplicationTheme {
        Greeting("Android")
    }
}*/





private fun checkAndRequestPermissions(activity: Activity) {
    val permissions = mutableListOf<String>()

    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            permissions.add(READ_MEDIA_IMAGES)
            permissions.add(READ_MEDIA_VIDEO)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> { // Android 10-12
            permissions.add(READ_EXTERNAL_STORAGE)
        }
        else -> { // Below Android 10
            permissions.add(READ_EXTERNAL_STORAGE)
            permissions.add(WRITE_EXTERNAL_STORAGE)
        }
    }

    if (permissions.isNotEmpty()) {
        ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), 102)
    }
}



private fun checkStoragePermission(activity: Activity): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(activity, READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}



/*private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val uri: Uri? = result.data?.data
        uri?.let {
            // Handle selected video URI
            videoView.setVideoURI(it) // Example: set in VideoView
            videoView.start()
        }
    }
}

// Function to open video picker
private fun openVideoPicker() {
    val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
    pickVideoLauncher.launch(intent)
}*/
