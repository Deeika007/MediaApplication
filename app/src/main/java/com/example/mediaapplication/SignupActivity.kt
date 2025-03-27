package com.example.mediaapplication

import android.content.Context
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun Signupscreen(/*onLoginClick:()-> Unit*/ auth: FirebaseAuth,
                context: Context,
                onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

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

                //UserListScreen(onLoginSuccess1)
                addDataToFirebase(email,password,auth,context)
            }) {
            Text(text = "Signup")

        }

    }


}

fun addDataToFirebase(
    email: String,
    password: String,
    auth: FirebaseAuth,
    context: Context
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
        signUpUser(email, password,auth,
            onSuccess = {
                Toast.makeText(context, "Signup successful! Check your email for verification", Toast.LENGTH_SHORT).show()
            },
            onFailure = { error -> message = error })

    }.addOnFailureListener { e ->
        Log.d("TAG", "addDataToFirebase: failure"+e.message)

    }

}

fun signUpUser(
    email: String,
    password: String,
    auth: FirebaseAuth,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("TAG", "signUpUser: calling")

                val userId = auth.currentUser?.uid
                Log.d("TAG", "signUpUser2: calling"+userId)

                sendEmailVerification(onSuccess, onFailure,auth)
            } else {
                onFailure(task.exception?.message ?: "Signup failed")
            }
        }

}

private fun sendEmailVerification(
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit,
    auth: FirebaseAuth
) {
    auth.currentUser?.sendEmailVerification()
        ?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()

            } else {
                onFailure(task.exception?.message ?: "Verification email failed")
            }
        }
}