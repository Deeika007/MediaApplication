package com.example.mediaapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContent {
              Signupscreen(auth,LocalContext.current) {
              }
        }

    }
}

@Composable
fun Signupscreen( auth: FirebaseAuth,
                context: Context,
                onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading = remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Top
    )

    {

        Text(text = "Signup",
            fontSize = 20.sp,
            modifier = Modifier.wrapContentWidth(),
            color = Color.Black

        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Email")
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Password")
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp)) // Space before the button

        Button(

            modifier = Modifier.fillMaxWidth(),


            onClick = {
                when {
                    email.isEmpty() -> {
                        Toast.makeText(context, "Email is empty!", Toast.LENGTH_SHORT).show()
                    }

                    !email.matches(emailRegex) -> {
                        Toast.makeText(context, "Invalid email format!", Toast.LENGTH_SHORT).show()
                    }

                    password.isEmpty() -> {
                        Toast.makeText(context, "Password is empty!", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        isLoading.value = true
                        addDataToFirebase(email,password,auth,context,isLoading = isLoading)
                    }
                }
            }) {
            if (isLoading.value) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(text = "Login")
            }
        }


    }


}

fun addDataToFirebase(
    email: String,
    password: String,
    auth: FirebaseAuth,
    context: Context,
    isLoading: MutableState<Boolean>
    /*courseDescription: String,
    context: Context*/
) {
    var message =""
    // on below line creating an instance of firebase firestore.
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    //creating a collection reference for our Firebase Firestore database.
    val dbsignup: CollectionReference = db.collection("signup")
    //adding our data to our courses object class.
    val signup = SignupModel(email, password)

    //below method is use to add data to Firebase Firestore.
    dbsignup.add(signup).addOnSuccessListener {
        Log.d("TAG", "addDataToFirebase: successfully signup")
        addDatattoDb(email, password,auth,context,isLoading = isLoading,
            onSuccess = {
                isLoading.value=false
                Log.d("TAG", "addDataToFirebase: success response")
            },
            onFailure = { error ->
                isLoading.value=false
                message = error
            })

    }.addOnFailureListener { e ->
        isLoading.value=false
        Log.d("TAG", "addDataToFirebase: failure"+e.message)

    }

}

fun addDatattoDb(
    email: String,
    password: String,
    auth: FirebaseAuth,
    context: Context,
    isLoading: MutableState<Boolean>,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance() //
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                Log.d("TAG", "signUpUser: userId $userId")

                if (userId != null) {
                    // Store user data in Firestore using UID as document ID
                    val userData = hashMapOf(
                        "email" to email,
                        "password" to password,
                    )
                    db.collection("signup").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            isLoading.value=false
                            Log.d("TAG", "User document created with ID: $userId")
                            sendEmailVerification(onSuccess, onFailure, auth, context,isLoading = isLoading,)
                        }
                        .addOnFailureListener { e ->
                            isLoading.value=false
                            Log.e("TAG", "Error adding document", e)
                            onFailure(e.message ?: "Failed to store user data")
                        }
                }
            } else {
                onFailure(task.exception?.message ?: "Signup failed")
            }
        }

}

private fun sendEmailVerification(
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit,
    auth: FirebaseAuth,
    context: Context,
    isLoading: MutableState<Boolean>
) {
    auth.currentUser?.sendEmailVerification()
        ?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
                isLoading.value=false
                    Toast.makeText(context, "Signup successful! Check your email for verification", Toast.LENGTH_SHORT).show()
                   val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)

            } else {
                isLoading.value=false
                onFailure(task.exception?.message ?: "Verification email failed")
            }
        }
}