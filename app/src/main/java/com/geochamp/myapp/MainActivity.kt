package com.geochamp.myapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.geochamp.myapp.Model.UserModel
import com.geochamp.myapp.View.LoginActivity
import com.geochamp.myapp.View.MapsActivity
import com.geochamp.myapp.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var dbRef: DatabaseReference
    //
    private lateinit var user_id: String
    private lateinit var user: UserModel
    //private var SPLASH_SCREEN_TIME_OUT:Long=2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //set splash screen
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        firebaseAuth = FirebaseAuth.getInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ///
//        Handler().postDelayed( Runnable() {
//
//            run() {
//                val intent= Intent(this,
//                    MapsActivity::class.java)
//                //Intent is used to switch from one activity to another.
//
//                startActivity(intent)
//                //invoke the SecondActivity.
//                finish()
//                //the current activity will get finished.
//            }
//        }, SPLASH_SCREEN_TIME_OUT)
        //init
        firebaseAuth = FirebaseAuth.getInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbRef = FirebaseDatabase.getInstance().getReference("UserModel")
        user_id = firebaseAuth.currentUser?.uid.toString()
        //go to map page
        binding.mainMapBtn.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        binding.mainLogoutBtn.setOnClickListener {
            //log out
            firebaseAuth.signOut()
            signOutGoogle()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }
    private fun signOutGoogle(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

    }


//    fun getUsersinfo() {
//
//
//        dbRef.child(user_id).addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                user= snapshot.getValue(UserModel::class.java)!!
//                binding.mainUserInfo.setText(user.firstName+" " +user.lastName+" " +user.personal_score.toString() )
//                // Log.e("zzzzzzzz","${user.personal_score}")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//        })
//
//
//    }



}