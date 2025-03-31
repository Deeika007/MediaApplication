package com.example.mediaapplication


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()

        val prefs: SharedPreferences = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        val isFirstTimeInstall = prefs.getBoolean(Constants.KEY_FIRST_TIME_INSTALL, false)
        val isLogin = prefs.getBoolean(Constants.KEY_ISLOGGIN, false)

        setContent {

            if(isLogin){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            else{
                LoginScreen(auth,LocalContext.current,isFirstTimeInstall,prefs){
                }

            }


        }
    }

}



@Composable
fun LoginScreen(auth: FirebaseAuth,
                context: Context,
                isFirstTimeInstall: Boolean,
                prefs: SharedPreferences,
                onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
  //  var isLoading by remember { mutableStateOf(false) }
    var isLoading = remember { mutableStateOf(false) } // ✅ Use MutableState<Boolean>
    var message by remember { mutableStateOf("") }

    val firestore = FirebaseFirestore.getInstance()

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
        Button(

            modifier = Modifier.fillMaxWidth(),
            onClick = {
                when {
                    email.isEmpty() -> {
                        Toast.makeText(context, "Email is empty!", Toast.LENGTH_SHORT).show()
                    }

                    password.isEmpty() -> {
                        Toast.makeText(context, "Password is empty!", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        isLoading.value = true // Show loader
                        validateUserLogin(
                            enteredEmail = email,
                            enteredPassword = password,isLoading = isLoading,
                            context,
                            onSuccess = {
                                isLoading.value = true // Stop loader
                              //  Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()



                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener { authResult ->

                                        val user = authResult.user
                                        if (user == null) {
                                           // onFailure("Authentication failed.")
                                            return@addOnSuccessListener
                                            isLoading.value=false

                                        }

                                        if (user != null) {
                                            Log.d("TAG", "User authenticated: ${user.email}")
                                        }

                                        // Check if email is verified
                                        if (user != null) {
                                            if (!user.isEmailVerified) {
                                                isLoading.value = false
                                                Toast.makeText(
                                                    context,
                                                    "Email not verified. Please verify your email.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                auth.signOut()
                                              //  onFailure("Email not verified.")
                                                return@addOnSuccessListener
                                            }
                                        }
                                        else{
                                            Log.d("TAG", "LoginScreen: calling")
                                        }

                                        // Fetch Firestore document ID (userId)
                                        firestore.collection("signup")
                                            .whereEqualTo("email", email)
                                            .get()
                                            .addOnSuccessListener { documents ->
                                                val documentSnapshot = documents.documents[0]
                                                val userId = documentSnapshot.id // ✅ Firestore Document ID

                                                // Store userId in SharedPreferences
                                                val preferenceHelper = PreferenceHelper()
                                                preferenceHelper.saveUserIdToPrefs(context, userId)

                                                Log.d("TAG", "Firestore userId: $userId")

                                                isLoading.value=false

                                                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                                if (!isFirstTimeInstall) {
                                                    prefs.edit().putBoolean(Constants.KEY_FIRST_TIME_INSTALL, true).apply()
                                                }
                                                prefs.edit().putBoolean(Constants.KEY_ISLOGGIN, true).apply()

                                                val intent = Intent(context, MainActivity::class.java)
                                                context.startActivity(intent)


                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("TAG", "Firestore error: ${e.message}")

                                            }
                                    }


                            },
                            onFailure = { error ->
                                isLoading.value = false // Stop loader on error
                                Toast.makeText(
                                    context,
                                    "User not registered. Please sign up to create an account before proceeding",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
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
    isLoading: MutableState<Boolean>,
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {


    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    fetchUsersFromFirestore(
        onSuccess = { userList ->
            val user = userList.find { it.email == enteredEmail }  // Find user by email

            if (user != null) {
                if (user.password == enteredPassword) {

                    isLoading.value=false

                    // ✅ Login successful
                    onSuccess()
                    Log.d("TAG", "validateUserLogin: success match")

                } else {
                    isLoading.value=false

                    Toast.makeText(
                        context,
                        "Your email and password does not match",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("TAG", "validateUserLogin: password does not match")
                }
            } else {
                isLoading.value=false

                Toast.makeText(
                    context,
                    "User not registered. Please sign up to create an account before proceeding",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("TAG", "User not registered. Please sign up to create an account before proceeding")
            }
        },
        onFailure = { exception ->
            isLoading.value=false
            Log.d("TAG", "validateUserLogin: failure response")
            onFailure("Error fetching users: ${exception.message}")
        }
    )
}


fun fetchUsersFromFirestore(
    onSuccess: (List<SignupModel>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val usersCollection = db.collection("signup")

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








