package com.geochamp.myapp.View

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.geochamp.myapp.Controller.LoginController
import com.geochamp.myapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

/**
 * LoginActivity is an Activity class that is used for handling the user login process.
 * The class uses FirebaseAuth and GoogleSignInClient to handle user authentication and sign-in.
 *
 * @property binding is an instance of ActivityLoginBinding that is used to bind the UI components with the code.
 * @property firebaseAuth is an instance of FirebaseAuth used for user authentication.
 * @property mGoogleSignInClient is an instance of GoogleSignInClient used for handling the Google Sign-In process.
 * @property loginController is an instance of LoginController that contains the logic for handling user login.
 */
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var loginController: LoginController
    //Game Timer
//    private var startTime: Long = 0
//    private var timer: Timer? = null
//    private var elapsedTime: Long = 0
//    private lateinit var userId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build())
        loginController = LoginController(this, binding, firebaseAuth, mGoogleSignInClient)
        loginController.onCreate()

    }

    override fun onStart() {
        super.onStart()
        loginController.onStart()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        loginController.onActivityResult(requestCode, resultCode, data)
    }

}