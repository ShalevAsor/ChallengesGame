package com.example.myapp.View

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import com.example.myapp.Model.MarkerModel
import com.example.myapp.R
import com.example.myapp.View.LoginActivity
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
        deleteMarkers()
//        deleteMarkersNow()

        // This is used to hide the status bar and make
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000) //2 sec in milli seconds
    }

//After a challenge is deleted, the number of players that played that challenge will be added to the creator of the challenge


    fun deleteMarkers(){

        val dbRef = FirebaseDatabase.getInstance().getReference("Markers")
        val markerListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (empSnap in dataSnapshot.children) {
                        val markerData = empSnap.getValue(MarkerModel::class.java)
                        val marker_id= markerData?.marker_id
                        val now = Calendar.getInstance().timeInMillis
                        val expiry = markerData?.time_to_live
                        val elapsed = now - expiry!!
                        val week = 24 * 60 * 60 * 1000 * 7
                        val markerRef=dbRef.child("$marker_id")
                        if(markerData!=null && elapsed> week){
                            if (marker_id != null) {
                                //addAfterDelete(marker_id)
                            }
                            markerRef.removeValue()

                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        dbRef.addValueEventListener(markerListener)
    }
    fun deleteMarkersNow(){

        val dbRef = FirebaseDatabase.getInstance().getReference("Markers")
        val markerListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (empSnap in dataSnapshot.children) {
                        val markerData = empSnap.getValue(MarkerModel::class.java)
                        val marker_id= markerData?.marker_id
                        val markerRef=dbRef.child("$marker_id")
                        if(markerData!=null){

                            markerRef.removeValue()

                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        dbRef.addValueEventListener(markerListener)
    }
}