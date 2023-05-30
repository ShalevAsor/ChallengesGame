package com.geochamp.myapp.Controller

import android.util.Log
import com.geochamp.myapp.Model.ScoreModel
import com.geochamp.myapp.Model.UserModel
import com.geochamp.myapp.View.UserStatsActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserStatsController(private val view: UserStatsActivity) {



    fun getUser(userID: String, userCallback: (user: UserModel?) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userID)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userData = dataSnapshot.value as Map<String, Any>?
                if (userData != null) {

                    val user = UserModel(
                        userData["firstName"] as String,
                        userData["lastName"] as String,
                        userData["userEmail"] as String,
                        userData["imageUrl"] as String?,
                        userData["personalScore"] as Long?,
                        userData["pass"] as String
                    )
                    userCallback(user)
                } else {
                    userCallback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("values", "Failed to read value.", error.toException())
            }
        })
    }


    fun getRank(
        topScores: MutableList<ScoreModel>,
        topScoresCallback: (user: MutableList<ScoreModel>?) -> Unit) {
        /* get the data of each score in the Billboard aka top scores */
        FirebaseDatabase.getInstance().reference.child("Billboard")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    /* if some of the scores has changed then it will update the adapter */
                    for (dss in snapshot.children) {
                        val scoreModel: ScoreModel? = dss.getValue(ScoreModel::class.java)
                        Log.i("GetRank:","$scoreModel")
                        if (scoreModel != null) {
                            topScores.add(scoreModel)
                        }
                    }
                    Log.i("thetopscoresis:","$topScores")

                    topScores.sortByDescending { it.total_score }
                    topScoresCallback(topScores)

                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}