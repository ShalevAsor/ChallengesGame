package com.example.myapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.myapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //allows to go to login page
        binding.loginPage.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }


        // set user information
        binding.btnSigUp.setOnClickListener{
            val email = binding.editEmail.text.toString()
            val fName = binding.editFName.text.toString()
            val lName = binding.editLName.text.toString()
            val pass = binding.editPass.text.toString()

            if(email.isNotEmpty() && fName.isNotEmpty() && lName.isNotEmpty() && pass.isNotEmpty()){
                //create user and go to login screen
                firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener{
                    if(it.isSuccessful){
                        val intent = Intent(this,LoginActivity::class.java)
                        startActivity(intent)
                    }
                    else{
                        Toast.makeText(this,it.exception.toString(),Toast.LENGTH_LONG).show()
                    }
                }


            }
            else {
                Toast.makeText(this,"empty fields",Toast.LENGTH_LONG).show()
            }

        }
    }
}