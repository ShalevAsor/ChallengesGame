package com.geochamp.myapp.View

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.geochamp.myapp.Model.MarkerModel
import com.geochamp.myapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

/**
 * This class `SplashScreen` represents the Splash Screen activity of the application.
 * This class is responsible for displaying a splash screen for 2 seconds when the application starts and then directing the user to the `LoginActivity`.
 */

@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // This is used to hide the status bar and make
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val b = Build.VERSION.SDK_INT
        val c = Build.VERSION_CODES.S


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000) //2 sec in milli seconds
    }

//After a challenge is deleted, the number of players that played that challenge will be added to the creator of the challenge



}