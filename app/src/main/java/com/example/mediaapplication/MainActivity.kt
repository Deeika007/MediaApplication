package com.example.mediaapplication

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {


    private lateinit var networkReceiver: NetworkReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        networkReceiver = NetworkReceiver()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, intentFilter)

        NetworkReceiver.isConnected.observe(this) { isConnected ->
            if (isConnected) {
                Toast.makeText(this, "Connected to Internet ✅", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No Internet Connection ❌", Toast.LENGTH_LONG).show()
            }
        }
        val prefs: SharedPreferences = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)

        val preferenceHelper = PreferenceHelper()
        val userId = preferenceHelper.getUserIdFromPrefs(this)

         val mediaViewModel: MediaViewModel by viewModels {
            MediaViewModelFactory(application,userId?:"")
        }


        Log.d("TAG", "onCreate: userid" + userId)
        mediaViewModel.getMediaFromFirestore(userId ?: "")

        setContent {

            MediaPickerScreen(viewModel = mediaViewModel,prefs)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MediaPickerScreen(viewModel: MediaViewModel, prefs: SharedPreferences) {


        val context = LocalContext.current
        val mediaList by viewModel.mediaList.collectAsState()
        val storageReference = FirebaseStorage.getInstance().reference
        val networkavailable = NetworkUtils(context)


        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val preferenceHelper = PreferenceHelper()

        val userId = preferenceHelper.getUserIdFromPrefs(context)


        val isFirstTimeInstall = prefs.getBoolean(Constants.KEY_FIRST_TIME_INSTALL, false)

        LaunchedEffect(Unit) {
            if (userId != null) {

                if(isFirstTimeInstall){
                    viewModel.getMediaFromFirestore(userId)
                    prefs.edit().putBoolean(Constants.KEY_FIRST_TIME_INSTALL, false).apply()
                }
                else{
                    viewModel.refreshMediaList(userId)
                }
            }
        }
       /* val pickMediaLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
              //  val formattedDate = getCurrentDate()
              //  var fileName = "Unknown"
             //   var fileSize: Long = 0
             //   fileName = uri?.let { getFileName(context, it) } ?: "Unknown"
              //  fileSize = uri?.let { getFileSize(context, it) }!!

                uri?.let {
                    val type = if (context.contentResolver.getType(it)
                            ?.startsWith("image/") == true
                    ) "image" else "video"

                    if (userId != null) {
                        viewModel.insertMedia(it.toString(), type,userId,"","",
                            "")
                    }
                    viewModel.refreshMediaList(userId?:"")

                    uploadMediaToFirebase(it, type, userId ?: "", context, viewModel)
                }
            }
        }
*/

        val pickMediaLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                val formattedDate = getCurrentDate()
                val fileName = uri?.let { getFileName(context, it) } ?: "Unknown"
                val fileSize = uri?.let { getFileSize(context, it) } ?: 0

                uri?.let {
                    val type = if (context.contentResolver.getType(it)?.startsWith("image/") == true) "image" else "video"

                    userId?.let { id ->
                        viewModel.insertMedia(it.toString(), type, userId, fileName, fileSize.toString(), formattedDate)
                    }
                    viewModel.refreshMediaList(userId?:"")
                    uploadMediaToFirebase(it, type, userId ?: "", context, viewModel)
                }
            }
        }


        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Media Picker") },
                    actions = {
                        Text(
                            text = "Logout",
                            color = Color.Blue,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable {

                                    val prefs: SharedPreferences = context.getSharedPreferences(
                                        Constants.PREF_NAME,
                                        Context.MODE_PRIVATE
                                    )
                                    val editor = prefs.edit()

                                    // ✅ Reset shared preferences
                                    editor.putBoolean(
                                        Constants.KEY_ISLOGGIN,
                                        false
                                    ) // Mark user as logged out
                                    editor.putBoolean(
                                        Constants.KEY_FIRST_TIME_INSTALL,
                                        false
                                    ) // Reset first-time flag
                                    editor.apply()

                                    val intent = Intent(context, LoginActivity::class.java)
                                    context.startActivity(intent)
                                    preferenceHelper.clearUserId(context)
                                    FirebaseAuth
                                        .getInstance()
                                        .signOut()
                                }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            floatingActionButton = {
                Button(
                    onClick = {
                        if (checkStoragePermission(context as Activity)) {
                            val intent = Intent(Intent.ACTION_PICK).apply {
                                type = "*/*"
                                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                            }
                            pickMediaLauncher.launch(intent)
                        } else {
                            checkAndRequestPermissions(context as Activity)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        //   .padding(start = 30.dp, end = 30.dp)
                        .padding(start = 30.dp)


                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                        contentColor = Color.White
                    )
                ) /*{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.file),
                            contentDescription = "Select Media",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Select Media", fontSize = 12.sp)
                    }
                }*/
                {
                    Text(text = "Select Media", fontSize = 12.sp)
                }
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(mediaList.size) { index ->
                            val media = mediaList[index]
                            val type = mediaList[index]
                            val name = mediaList[index]
                            val size = mediaList[index]
                            val created = mediaList[index]

                            MediaItem(media.uri, type.type, name.name,size.size,created.created)
                        }
                    }
                }
            }
        )
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0
        } catch (e: Exception) {
            Log.e("FileSize", "Error getting file size: ${e.message}")
            0
        }
    }

    fun getCurrentDate(): String {

        val timeStamp = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timeStamp))
    }

    fun getFileFromContentUri(context: Context, contentUri: Uri): Uri? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        context.contentResolver.query(contentUri, filePathColumn, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val filePath = cursor.getString(columnIndex)
                return Uri.fromFile(File(filePath))
            }
        }
        return null
    }
    fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }


    @Composable
    fun MediaItem(mediaUri: String, type: String, name: String, size: String, created: String) {

        val context = LocalContext.current
        val retriever = remember { MediaMetadataRetriever() }
        var bitmap by remember { mutableStateOf<Bitmap?>(null) }

        /*  LaunchedEffect(mediaUri) {
        if (mediaUri.endsWith(".mp4") || type.contains("video")) {
            try {
                Log.d("TAG", "MediaItem: mediauri" +mediaUri)

                val uri = Uri.parse(mediaUri)
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    retriever.setDataSource(pfd.fileDescriptor)
                    bitmap = retriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                }

            } catch (e: Exception) {
                Log.e("MediaItem", "Error retrieving video frame", e)
            } finally {
                retriever.release()
            }
        }
    }*/

        LaunchedEffect(mediaUri) {
            if (type.contains("video")) {
                try {
                    if (mediaUri.isNullOrEmpty()) {
                        Log.e("MediaItem", "Invalid mediaUri: $mediaUri")
                        return@LaunchedEffect
                    }

                    val uri = Uri.parse(mediaUri)
                    val copiedFile = copyFileToInternalStorage(context, uri, "copied_video.mp4")

                    if (copiedFile != null) {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(copiedFile.absolutePath)

                        val frame = retriever.getFrameAtTime(
                            1000000,
                            MediaMetadataRetriever.OPTION_CLOSEST
                        ) // Extract frame at 1 sec
                        frame?.let {
                            bitmap = it
                            saveBitmapToInternalStorage(
                                it,
                                "thumbnail.jpg",
                                context
                            ) // Save persistently
                        } ?: Log.e("MediaItem", "Failed to retrieve frame")

                        retriever.release()
                    } else {
                        Log.e("MediaItem", "Failed to copy video file")
                    }

                } catch (e: Exception) {
                    Log.e("MediaItem", "Error retrieving video frame: ${e.localizedMessage}", e)
                }
            }
        }


        Card(
            modifier = Modifier
                .padding(4.dp)
                .size(120.dp),
            shape = RoundedCornerShape(8.dp),
            onClick = {

                val intent = Intent(context, MediaPreviewActivity::class.java).apply {
                    putExtra("media_uri", mediaUri) // Image or video path
                    putExtra("media_type", type) // "image" or "video"
                     putExtra("file_name", name)
                      putExtra("file_size", size)
                    putExtra("file_date", created)
                }
                context.startActivity(intent)
            },
            colors = CardDefaults.cardColors(containerColor = Color.LightGray)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (type.contains("video") && bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Video Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(mediaUri),
                        contentDescription = "Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                if (type.contains("video")) {
                    IconButton(
                        onClick = { /* TODO: Handle Play Video Click */ },
                        modifier = Modifier
                            .size(32.dp) // Adjust circle size
                            .clip(CircleShape) // Ensures circular shape
                            .background(Color.Black.copy(alpha = 0.5f)) // Custom background
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.group_59),
                            contentDescription = "Play Video",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp) // Adjust icon size
                        )
                    }
                }
            }
        }
    }

    fun uploadMediaToFirebase(
        uri: Uri,
        type: String,
        userId: String,
        context: Context,
        viewModel: MediaViewModel
    ) {
        Log.d("TAG", "uploadMediaToFirebase: userId" + userId + "type " + type + " uri" + uri)
        val timestamp = System.currentTimeMillis()


        val formattedDate = getCurrentDate()
        val fileName = uri.let { getFileName(context, it) } ?: "Unknown"
        val fileSize = uri.let { getFileSize(context, it) } ?: 0

      //  Log.d("TAG", "uploadMediaToFirebase: formattedDate"+formattedDate+"fileName "+fileName+" fileSize "+fileSize)

        val storageRef = FirebaseStorage.getInstance().reference.child("signup/$userId/$timestamp")
        storageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->

                    saveMediaToFirestore(downloadUri.toString(), type, userId,formattedDate,fileName,fileSize.toString())
                    Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()
                    Log.d(
                        "TAG",
                        "uploadMediaToFirebase: downloadUri.toString()" + downloadUri.toString()
                    )

                  /*  viewModel.insertMedia(
                        downloadUri.toString(),
                        type*//*,timestamp*//*
                    )  // Update local list*/
                }
            }
            .addOnFailureListener {
                Log.d("TAG", "uploadMediaToFirebase: failure" + it.message)
                Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun saveMediaToFirestore(
        mediaUrl: String,
        type: String,
        userId: String?,
        formattedDate: String,
        fileName: String,
        fileSize: String
    ) {
        if (userId.isNullOrEmpty()) {
            Log.e("Firestore", "Error: userId is null or empty")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val mediaData = hashMapOf(
            "url" to mediaUrl,
            "type" to type,
            "Created" to formattedDate,
            "name" to fileName,
            "size" to fileSize

        )

        db.collection("signup").document(userId)
            .collection("media")
            .document() // ✅ Generates a unique ID for each media entry
            .set(mediaData)
            .addOnSuccessListener {
                Log.d("Firestore", "Media saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving media: ${e.message}")
            }
    }


    fun saveBitmapToInternalStorage(bitmap: Bitmap, fileName: String, context: Context): Boolean {
        return try {
            val file = File(context.filesDir, fileName) // Save in internal storage
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos) // Compress and save as JPEG
            }
            Log.d("MediaItem", "Thumbnail saved successfully: ${file.absolutePath}")
            true
        } catch (e: IOException) {
            Log.e("MediaItem", "Error saving thumbnail: ${e.localizedMessage}", e)
            false
        }
    }

    fun copyFileToInternalStorage(context: Context, uri: Uri, fileName: String): File? {
        val destinationFile = File(context.filesDir, fileName) // Save in internal storage

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return destinationFile
        } catch (e: IOException) {
            Log.e("MediaItem", "Error copying file to internal storage: ${e.localizedMessage}", e)
        }
        return null
    }


    private fun checkAndRequestPermissions(activity: Activity) {
        val permissions = mutableListOf<String>()

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                permissions.add(READ_MEDIA_IMAGES)
                permissions.add(READ_MEDIA_VIDEO)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                permissions.add(READ_EXTERNAL_STORAGE)
            }

            else -> {
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
            ContextCompat.checkSelfPermission(
                activity,
                READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        activity,
                        READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                activity,
                READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

