package com.geochamp.myapp.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.geochamp.myapp.Controller.RegisterController
import com.geochamp.myapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * Class RegisterActivity is an activity for user registration.
 * It consists of an interface for the user to input their information
 * and perform the registration.
 *
 * @property binding an instance of the ActivityRegisterBinding to inflate the layout
 * @property firebaseAuth an instance of FirebaseAuth to perform authentication operations
 * @property dbRef an instance of DatabaseReference to reference the UserModel in the Firebase Database
 * @property dbRef_2 an instance of DatabaseReference to reference the Billboard in the Firebase Database
 * @property registerController an instance of RegisterController to handle the registration process
 */
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var dbRef_2: DatabaseReference
    private lateinit var registerController: RegisterController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef_2 = FirebaseDatabase.getInstance().getReference("Billboard")

        registerController = RegisterController(binding, firebaseAuth, dbRef, dbRef_2)


    }

    override fun onStart() {
        super.onStart()

        binding.registerPage.setOnClickListener {
            registerController.goToLogin()
        }

        binding.btnSignUp.setOnClickListener {
            registerController.registerUser()
        }
    }
}
