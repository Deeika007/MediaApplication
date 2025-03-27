package com.example.mediaapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
/*import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener*/
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContent {
            /*  MediaApplicationTheme {
                  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                      Greeting(
                          name = "Android",
                          modifier = Modifier.padding(innerPadding)
                      )
                  }
              }*/

            LoginScreen(auth,LocalContext.current){

                  /*val intent = Intent(this, MainActivity::class.java)
                  startActivity(intent)*/
            }

          //  UserListScreen()

        }
    }
    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Close LoginActivity
    }
}



@Composable
fun LoginScreen(/*onLoginClick:()-> Unit*/ auth: FirebaseAuth,
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

        Text(text = "Login",
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


        /*    Button(
            onClick = {
                if (email.isBlank()) {
                    message = "Please enter your email."
                    return@Button
                }

                isLoading = true

                // Generate a random temporary password
                val tempPassword = UUID.randomUUID().toString().take(8)

                // Create or update the user with this password
                auth.createUserWithEmailAndPassword(email, tempPassword)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            message = "Temporary password sent to $email."
                            sendEmail(email, tempPassword) // Send the password via email
                        } else {
                            message = "Error: ${task.exception?.localizedMessage}"
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isLoading) "Sending..." else "Apply")
        }

        if (message.isNotEmpty()) {
            Text(text = message, color = Color.Red)
        }
    }*/
        Button(

            modifier = Modifier.fillMaxWidth(),

            onClick = {
                validateUserLogin(
                    enteredEmail = email,
                    enteredPassword = password,context,
                    onSuccess = {
                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                        // Navigate to the next screen (if needed)
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)

                    },
                    onFailure = { error ->
                        Log.d("TAG", "LoginScreen: error"+error)
                    }
                )
            }) {
            Text(text = "Login")

        }
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Don't have an account",
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp)) // Small space between the texts

            Text(
                text = "SIGN UP",
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    // Navigate to SignupActivity
                    val intent = Intent(context, SignupActivity::class.java)
                    context.startActivity(intent)
                }
            )
        }


    }


}

fun validateUserLogin(
    enteredEmail: String,
    enteredPassword: String,
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    fetchUsersFromFirestore(
        onSuccess = { userList ->
            val user = userList.find { it.email == enteredEmail }  // Find user by email

            if (user != null) {
                if (user.password == enteredPassword) {
                    // ✅ Login successful
                    onSuccess()
                } else {
                    // ❌ Password does not match
                    Toast.makeText(
                        context,
                        "Your email and password does not match",
                        Toast.LENGTH_SHORT
                    ).show()
                   // onFailure("Password does not match")
                }
            } else {
                // ❌ Email not found
                Toast.makeText(
                    context,
                    "User not registered. Please sign up to create an account before proceeding",
                    Toast.LENGTH_SHORT
                ).show()

            }
        },
        onFailure = { exception ->
            onFailure("Error fetching users: ${exception.message}")
        }
    )
}

@Composable
fun UserListScreen() {
    val signupUsers = remember { mutableStateListOf<SignupModel>() }

    LaunchedEffect(Unit) {
        fetchUsersFromFirestore(
            onSuccess = { userList ->
                signupUsers.clear()
                signupUsers.addAll(userList)
            },
            onFailure = { error ->
                Log.e("FirebaseError", error.message ?: "Unknown Error")
            }
        )
    }

    LazyColumn {
        items(signupUsers.toList()) { user ->  // ✅ Convert to List
            UserItem(user)
        }
    }
}




@Composable
fun UserItem(user: SignupModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(text = "password: ${user.password}", fontWeight = FontWeight.Bold)
        Text(text = "Email: ${user.email}", color = Color.Gray)
    }
}

fun fetchUsersFromFirestore(
    onSuccess: (List<SignupModel>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val usersCollection = db.collection("signup")  // Ensure this matches your Firestore collection name

    usersCollection.get()
        .addOnSuccessListener { result ->
            val userList = mutableListOf<SignupModel>()
            for (document in result) {
                val user = document.toObject(SignupModel::class.java)
                userList.add(user)

                Log.d("TAG", "fetchUsersFromFirestore: userlist"+userList)
            }
            onSuccess(userList)
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}

/*fun addDataToFirebase(
    email: String,
    password: String,
    auth: FirebaseAuth
    *//*courseDescription: String,
    context: Context*//*
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
        // after the data addition is successful
        // we are displaying a success toast message.
        *//* Toast.makeText(
             context,
             "Your Course has been added to Firebase Firestore",
             Toast.LENGTH_SHORT
         ).show()*//*
        signUpUser(email, password,auth,
            onSuccess = { message = "Signup successful! Check your email for verification." },
            onFailure = { error -> message = error })

    }.addOnFailureListener { e ->
        Log.d("TAG", "addDataToFirebase: failure"+e.message)
        // this method is called when the data addition process is failed.
        // displaying a toast message when data addition is failed.
        //  Toast.makeText(context, "Fail to add course \n$e", Toast.LENGTH_SHORT).show()
    }

}*/

/*fun signUpUser(
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

                *//*   if (userId != null) {

                       Log.d("TAG", "signUpUser3: calling"+userId)
                       saveUserToFirestore(userId, email, password, {
                           sendEmailVerification(onSuccess, onFailure, auth) // Call email verification after saving data
                       }, onFailure)
                   } else {
                       onFailure("User ID is null")
                   }*//*


                sendEmailVerification(onSuccess, onFailure,auth)
            } else {
                onFailure(task.exception?.message ?: "Signup failed")
            }
        }

}*/




/*
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
*/

fun loginUser(
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()

    // Fetch sign-in methods for the provided email
    auth.fetchSignInMethodsForEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods ?: emptyList()
                Log.d("LoginDebug", "Fetched sign-in methods for email $email: $signInMethods")


                if (signInMethods.isEmpty()) {
                    // Email is not found in Firebase Authentication
                    Log.d("TAG", "loginUser: method is empty")
                    onFailure("Please sign up first.")
                } else {
                    // Email exists, now try signing in
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { loginTask ->
                            if (loginTask.isSuccessful) {
                                val user = auth.currentUser
                                if (user?.isEmailVerified == true) {
                                    println("successfully login")
                                    onSuccess()
                                } else {
                                    onFailure("Please verify your email before logging in.")
                                }
                            } else {
                                onFailure("Password mismatch.")
                            }
                        }
                }
            } else {
                onFailure("Error checking email: ${task.exception?.message}")
            }
        }
}




