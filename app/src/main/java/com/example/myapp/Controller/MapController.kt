package com.example.myapp.Controller

import android.location.Location
import com.example.myapp.View.MapsActivity
import com.example.myapp.Model.Callback
import com.example.myapp.Model.MarkerModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * MapController is a class responsible for managing markers data and performing operations related to markers such as
 * getting markers, adding markers, calculating distance between two points, and getting a user's score for a specific challenge.
 *
 * @param view: MapsActivity, an instance of the MapsActivity class.
 *
 * @property databaseReference: Reference, a reference to the Firebase Database.
 *
 */
class MapController(private val view: MapsActivity) {
    private val databaseReference = FirebaseDatabase.getInstance().reference

    /**
     * getMarkers retrieves the list of markers from Firebase Database and returns it as a list of MarkerModel objects.
     *
     * @return List<MarkerModel>, a list of MarkerModel objects.
     */
    fun getMarkers(): List<MarkerModel>  {
        val markers_list = arrayListOf<MarkerModel>()
        val dbRef = FirebaseDatabase.getInstance().getReference("Markers")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (empSnap in snapshot.children) {
                        val userData = empSnap.getValue(MarkerModel::class.java)
                        if(userData!=null)
                        {
                            markers_list.add(userData!!)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return markers_list
    }
    /**
     * addMarker adds a new marker to the Firebase Database.
     *
     * @param markerTag: String, a unique identifier for the marker.
     * @param challengeName: String, the name of the challenge associated with the marker.
     * @param challengeDescription: String, a description of the challenge associated with the marker.
     * @param lat: Double, the latitude of the marker.
     * @param long: Double, the longitude of the marker.
     * @param topScore: Int, the highest score for the challenge associated with the marker.
     * @param timeToLive: Int, the time limit for the challenge associated with the marker.
     *
     */
     fun addMarker(
        markerTag: String,
        challengeName: String,
        challengeDescription: String,
        lat: Double,
        long: Double,
        topScore: Int,
        timeToLive: Int
    ) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Markers")
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val marker = MarkerModel(markerTag, challengeName, challengeDescription, lat, long, topScore, timeToLive)
        dbRef.child(markerTag).setValue(marker)
        dbRef.child(markerTag).child("user_scores").child(userId).setValue(0)
    }

    /**
     * This method calculates the distance between two locations.
     *
     * @param currLatLng: LatLng, current location.
     * @param markerLatLng: LatLng, marker location.
     *
     * @return Double, distance between the two locations.
     */

     fun calculateDistance(currLatLng: LatLng, markerLatLng: LatLng): Double {
        val result = FloatArray(1)
        Location.distanceBetween(currLatLng.latitude, currLatLng.longitude,
            markerLatLng.latitude, markerLatLng.longitude, result)
        return result[0].toDouble()
    }

    /**
     * This method retrieves the score of a user for a specific challenge.
     * @param markerId id of the marker representing the challenge
     * @param userId id of the user whose score is being retrieved
     * @param callback callback that returns the score of the user for the challenge
     */

     fun getUserScoreForChallenge(markerId: String?, userId: String, callback: Callback) {
        val ref = FirebaseDatabase.getInstance().getReference("Markers/$markerId/user_scores/$userId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val score = dataSnapshot.value as? Long
                callback.onSuccess(score?.toInt() ?: 0)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onSuccess(0)
            }
        })
    }

}