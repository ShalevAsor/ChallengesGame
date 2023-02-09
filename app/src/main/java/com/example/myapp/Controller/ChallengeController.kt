package com.example.myapp.Controller

import android.app.Activity
import com.google.firebase.database.*

/**
 * Class ChallengeController updates the scores of a user and a marker aka challenge , also this class update
 * the billboard data in Firebase Database.
 *
 * @param view the Activity which instantiates the ChallengeController
 */

class ChallengeController(private val view: Activity) {


    /**
     * Method updateBillboard updates the total score of the user in the Billboard table.
     *
     * @param userId the unique identifier of the user
     * @param newScore the score to be added to the total score of the user
     */

    fun updateBillboard(userId: String, newScore: Int) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Billboard").child(userId)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var oldScore: Long = 0
                    if (dataSnapshot.child("total_score").value != null) {
                        oldScore = dataSnapshot.child("total_score").value as Long
                    }
                    reference.child("total_score").setValue(oldScore + newScore)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    /**
     * Method updateUserPersonalScore updates the personal score of the user in the Users table.
     *
     * @param userId the unique identifier of the user
     * @param newScore the score to be added to the personal score of the user
     */

    fun updateUserPersonalScore(userId: String, newScore: Int) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Users").child(userId)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val currentScore = dataSnapshot.child("personalScore").value.toString().toLong()
                    reference.child("personalScore").setValue(currentScore + newScore)
                } else {
                    reference.child("personalScore").setValue(newScore)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    /**
     * Method updateMarkerScore updates the score of the user for a specific marker in the Markers table.
     *
     * @param markerId the unique identifier of the marker
     * @param userId the unique identifier of the user
     * @param newScore the score to be added to the score of the user for the marker
     */

    fun updateMarkerScore(markerId: String, userId: String, newScore: Int) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Markers").child(markerId).child("user_scores").child(userId)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val currentScore = dataSnapshot.value as Long

                    if(currentScore<newScore){
                        reference.setValue(newScore)
                    }
                } else {
                    reference.setValue(newScore)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }


    /**
     * Method updateMarkerTopScore updates the top score of a marker in the Markers table.
     *
     * @param markerId the unique identifier of the marker
     * @param newScore the score to be added as the top score for the marker
     */

    fun updateMarkerTopScore(markerId: String, newScore: Int) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Markers").child(markerId).child("top_score")

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentScore = dataSnapshot.getValue(Int::class.java) ?: 0
                if (newScore > currentScore) {
                    reference.setValue(newScore)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    /**
     * Method updateAllScores updates the personal score, marker score, and marker top score of a user in Firebase Database.
     *
     * @param userId the unique identifier of the user
     * @param markerId the unique identifier of the marker
     * @param score the score to be added to the personal score, marker score, and marker top score of the user
     */

    fun updateAllScores(userId: String,markerId: String,score: Int ){
        updateUserPersonalScore(userId,score)
        updateMarkerScore(markerId,userId,score)
        updateMarkerTopScore(markerId,score)
    }

}