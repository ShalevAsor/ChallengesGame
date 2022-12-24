package com.example.myapp

import android.Manifest
import android.R.attr
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth


    //Google log in
    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var googleAccount: GoogleSignInAccount

    private val permissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        // login with google
        gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        //allow to go to register page
        binding.registerPage.setOnClickListener {
            updateUI("register")
        }
        binding.btnLogin.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val pass = binding.loginPass.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "empty fields", Toast.LENGTH_LONG).show()
            }
        }
        //google sign in
        binding.signInButton.setOnClickListener {
            signInGoogle()

        }
        requestLocationPermission()

    }

    //if user is logged in go to main activity
    override fun onStart() {
        super.onStart()
        //if the user is logged in already - go to main page
        if (firebaseAuth.currentUser != null || GoogleSignIn.getLastSignedInAccount(this) != null) {
            updateUI("main")
        }


    }


    private fun signInGoogle() {
        val intent = mGoogleSignInClient.signInIntent
        startActivityForResult(intent, 1);

    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {

        val account = completedTask.getResult(ApiException::class.java)
        updateUI("main")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 1 ) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    /* This function start different activities depends on the given string
    * main
    * register
    * login
    * */
    private fun updateUI(activity: String) {


        if (activity == "main") { // go to register
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        if (activity == "register") {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        if (activity == "login") {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
    /*
     * Request location permission,not handling the results here
     * its done in the MapsActivity by a callback
     */
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissionCode)
        }
    }
}