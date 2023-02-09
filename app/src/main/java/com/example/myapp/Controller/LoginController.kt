package com.example.myapp.Controller

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.myapp.View.LoginActivity
import com.example.myapp.View.MapsActivity
import com.example.myapp.View.RegisterActivity
import com.example.myapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

/**
 * Class LoginController is a class that handles the functionality of the Login page.
 * It is responsible for handling the user authentication using email and password
 * or using Google Sign-In.
 *
 * @property activity the LoginActivity instance
 * @property binding the ActivityLoginBinding instance
 * @property firebaseAuth the FirebaseAuth instance
 * @property mGoogleSignInClient the GoogleSignInClient instance
 */

class LoginController(private val activity: LoginActivity, private val binding: ActivityLoginBinding, private val firebaseAuth: FirebaseAuth, private val mGoogleSignInClient: GoogleSignInClient) {

    /**
     * Initializes the onClickListeners for the buttons in the LoginActivity and requests the location permission.
     */

    fun onCreate() {
        binding.registerPage.setOnClickListener {
            updateUI(activity,"register")
        }
        binding.btnLogin.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val pass = binding.loginPass.text.toString()
            loginWithEmailAndPassword(email, pass)
        }
        binding.signInButton.setOnClickListener {
            signInWithGoogle()
        }
        requestLocationPermission()
    }
    /**
     * If a user is already signed in, either with email/password or Google Sign In, updates the UI to the map activity.
     */

    fun onStart() {
        if (firebaseAuth.currentUser != null || GoogleSignIn.getLastSignedInAccount(activity) != null) {
            updateUI(activity,"map")
        }
    }
    /**
     * Handles the result of the Google Sign In activity.
     *
     * @param requestCode: request code passed to the activity result
     * @param resultCode: result code returned from the activity result
     * @param data: data passed back from the activity result
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }
    /**
     * Starts the Google Sign In activity.
     */
    private fun signInWithGoogle() {
        val intent = mGoogleSignInClient.signInIntent
        activity.startActivityForResult(intent, 1)
    }
    /**
     * Handles the result of the Google Sign In activity.
     * If the user is already signed in, signs in with email and password.
     * If not, creates a new user with email and password.
     *
     * @param completedTask: task containing the result of the Google Sign In activity
     */
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account?.email as String
            // TODO: Replace the hardcoded password with a strong password generator
            val password = "123456"
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateUI(activity,"map")
                    } else {
                        createUserWithEmailAndPassword(email, password)
                    }
                }
        } catch (e: ApiException) {
            Log.e("LoginController", "handleSignInResult: ${e.message}")
        }
    }

    /**
     * This method updates the UI based on the activity name passed as the argument.
     *
     * @param context The context of the current activity.
     * @param activity The name of the activity to navigate to.
     */
    fun updateUI(context: Context, activity: String) {
        if (activity == "register") {
            val intent = Intent(context, RegisterActivity::class.java)
            context.startActivity(intent)
        }
        if (activity == "login") {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
        if (activity == "map") {
            val intent = Intent(context, MapsActivity::class.java)
            context.startActivity(intent)
        }
    }
    /**
     * This method requests the location permission.
     */
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }
    }
    /**
     * This method logs in the user with the given email and password.
     *
     * @param email The email of the user.
     * @param password The password of the user.
     */
    private fun loginWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateUI(activity,"map")
                } else {
                    Toast.makeText(activity, "Error: ${task.exception?.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
    /**
     * This method creates a new user with the given email and password.
     *
     * @param email The email of the user.
     * @param password The password of the user.
     */
    private fun createUserWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateUI(activity,"map")
                } else {
                    Toast.makeText(activity, "Error: ${task.exception?.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
}