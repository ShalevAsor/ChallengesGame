package com.geochamp.myapp.Controller

import android.util.Log
import com.geochamp.myapp.Model.ScoreModel
import com.geochamp.myapp.Model.UserDataManager
import com.geochamp.myapp.Model.UserModel
import com.geochamp.myapp.View.TopScores
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * TopScoresController class handles the interaction between the TopScores view and the underlying data sources (Firebase database).
 * It retrieves the data from the Firebase database, processes it and updates the view with the data.
 *
 * @property view the TopScores view
 * @property userDataManager instance of UserDataManager to manage user data
 * @property scoresRef Firebase reference to the "Billboard" node in the database
 * @property top3Scores a mutable list to store the top 3 scores
 */
class TopScoresController(private val view: TopScores) {
    //tools
    private val userDataManager = UserDataManager()
    private val scoresRef = FirebaseDatabase.getInstance().getReference("Billboard")
    private val top3Scores = mutableListOf<ScoreModel>()


    /**
     * getUser method retrieves the user data from the Firebase database given a userID.
     * @param userID id of the user
     * @param userCallback callback that returns the user data or null if the user is not found
     */

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
    /**
     * getTop3Scores method retrieves the top 3 scores from the Firebase database and returns the data through the listener callback.
     * @param listener callback that returns the top 3 scores
     */
    fun getTop3Scores(listener: (List<ScoreModel>) -> Unit) {
        scoresRef.orderByChild("total_score").limitToLast(3).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val top3Scores = mutableListOf<ScoreModel>()

                for (scoreSnapshot in dataSnapshot.children.reversed()) {
                    val scoreModel = scoreSnapshot.getValue(ScoreModel::class.java)

                    if (scoreModel != null) {
                        Log.d("billboardvalues", "User created in the database${scoreModel.total_score}.")
                        top3Scores.add(scoreModel)
                    }
                }

                listener(top3Scores)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }
    /**
     * This method reads the top scores from the database and updates the adapter with the scores.
     *
     * @param topScores - A mutable list of `ScoreModel` to store the scores retrieved from the database.
     * @param topScoresAdapter - An adapter used to display the scores in a list.
     */
     fun readTopScores(
        topScores: MutableList<ScoreModel>,
        topScoresAdapter: TopScoresAdapter
    ) {
        /* get the data of each score in the Billboard aka top scores */
        FirebaseDatabase.getInstance().reference.child("Billboard")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    /* if some of the scores has changed then it will update the adapter */
                    for (dss in snapshot.children) {
                        val scoreModel: ScoreModel? = dss.getValue(ScoreModel::class.java)
                        if (scoreModel != null) {
                            topScores.add(scoreModel)
                        }
                    }
                    topScores.sortByDescending { it.total_score }
                    if (topScores.size > 3) {
                        topScores.subList(0, 3).clear() // Remove the first 3 values from the list
                    }
                    topScoresAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {}
            })
    } fun getRank(
        topScores: MutableList<ScoreModel>,
        //topScoresAdapter: TopScoresAdapter
    ) {
        /* get the data of each score in the Billboard aka top scores */
        FirebaseDatabase.getInstance().reference.child("Billboard")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    /* if some of the scores has changed then it will update the adapter */
                    for (dss in snapshot.children) {
                        val scoreModel: ScoreModel? = dss.getValue(ScoreModel::class.java)
                        if (scoreModel != null) {
                            topScores.add(scoreModel)
                        }
                    }
                    topScores.sortByDescending { it.total_score }

//                    if (topScores.size > 3) {
//                        topScores.subList(0, 3).clear() // Remove the first 3 values from the list
//                    }
                   // topScoresAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}




