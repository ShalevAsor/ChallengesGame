package com.geochamp.myapp.Controller

import android.app.Activity
import android.util.Log
import com.geochamp.myapp.Model.UserModel
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

    fun updateUserPersonalScore(userId: String,markerId: String, newScore: Int) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Users")
        val markersReference=database.getReference("Markers").child(markerId)
        Log.e("flag123", "i am outside")
        markersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.e("checkbug", "inside!")
                    val creatorId = dataSnapshot.child("challenge_creator").getValue(String::class.java)
                    val topScore=dataSnapshot.child("top_score").value.toString().toLong()
                    Log.e("checkbug", "inside2!")
                    userId?.let { id ->
                        reference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {

                                    val challengePlayed =snapshot.child("challengesPlayed").value.toString().toInt()

                                    val currentScore =snapshot.child("personalScore").value.toString().toLong()
                                    val currentEarnedPoints =snapshot.child("pointsEarned").value.toString().toLong()

                                    //Each time a game ends the value of challenges played by the user will add up by 1
                                    //Each time a game ends the value of earned points will add up by the new score

                                    reference.child(userId).child("challengesPlayed").setValue(challengePlayed + 1)
                                    reference.child(userId).child("personalScore").setValue(currentScore +0)
                                    Log.e("checkbug", "before the if")
                                    if(userId!=creatorId){

                                    }
                                    if (userId != creatorId) {
                                        Log.e("simple!", "inside firs if ")
                                        if (newScore > topScore) {
                                            reference.child(userId).child("pointsEarned")
                                                .setValue(currentEarnedPoints + newScore)
                                            reference.child(userId).child("personalScore").setValue(currentScore + newScore)
                                            updateBillboard(userId, newScore)
                                        }
//                                        else{
//                                             Log.e("simple!", "inside else")
//                                             reference.child(userId).child("pointsEarned").setValue(currentEarnedPoints + 1)
//                                         }
                                    }


                                } else {
                                    reference.child("personalScore").setValue(newScore)
                                }
                            }


                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }











    /**
     * Method updateCreatorScore updates the creator of the  challenge by 1 for each player that played the game.
     *
     * @param userId the unique identifier of the user
     * @param newScore the score to be added to the personal score of the user
     */

    fun updateCreatorScore(userId: String,markerId: String) {
        val database = FirebaseDatabase.getInstance()
        val markersReference = database.getReference("Markers").child(markerId)
        val usersReference = database.getReference("Users")

        markersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val creatorId = dataSnapshot.child("challenge_creator").getValue(String::class.java)
                    creatorId?.let { id ->
                        usersReference.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    if (userId != id) {
                                        Log.e("how?", "i am inside")
                                        val userTotalScore = snapshot.child("personalScore")
                                            .getValue(Int::class.java) ?: 0
                                        val userEarnedPts = snapshot.child("pointsEarned")
                                            .getValue(Int::class.java) ?: 0
                                        val updatedEarnedPts=userEarnedPts+1
                                        val updatedScore = userTotalScore + 1
                                        usersReference.child(id).child("personalScore")
                                            .setValue(updatedScore)
                                        usersReference.child(id).child("pointsEarned")
                                            .setValue(updatedEarnedPts)
                                        updateBillboard(id, 1)
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })
                    }
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
        updateCreatorScore(userId,markerId)
        updateUserPersonalScore(userId,markerId,score)
        updateMarkerScore(markerId,userId,score)
        updateMarkerTopScore(markerId,score)
        //updateBillboard(userId,score)
    }

}