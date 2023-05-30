package com.geochamp.myapp.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import com.geochamp.myapp.Controller.TopScoresController
import com.geochamp.myapp.Controller.UserProfileController
import com.geochamp.myapp.Controller.UserStatsController
import com.geochamp.myapp.Model.ScoreModel
import com.geochamp.myapp.databinding.ActivityUserStatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserStatsActivity : AppCompatActivity() {

    private lateinit var binding:ActivityUserStatsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var userId: String
    private lateinit var userProfileController: UserProfileController
    private lateinit var userStatsController: UserStatsController

    //user Rank
    private lateinit var topScoresController: TopScoresController
    private lateinit var topScores: MutableList<ScoreModel>
    var firstName: String? = null
    var totalScore: Long? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        supportActionBar?.title = "Geo-Champ"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        userStatsController = UserStatsController(this)

        topScores = ArrayList()

        firebaseAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("Markers")
        userId = firebaseAuth.currentUser!!.uid


        //Get user Rank
        if (userId != null) {
            userStatsController.getUser(userId) { user ->
                if (user != null) {
                    firstName = user.firstName
                    totalScore = user.personalScore
                    //Log.d("signgs", "the value is - $totalScore.")
                }
            }
        }
        userStatsController.getRank(topScores) { list ->

            Log.i("Topscores", "$list , ")
            Log.i("topscoresvalues", "fname:$firstName , ts:$totalScore")

            val filteredList =
                list?.filter { it.firstName == firstName.toString() && it.total_score == totalScore }
            Log.i("filterlist", "$filteredList , ")
            if (filteredList != null) {
                if (filteredList.isEmpty()) {
                    Log.d("errorRank", "no such player was found")
                }
            }
            val foundScoreModel = filteredList?.first()
            val userRank: Int = topScores.indexOf(foundScoreModel) + 1


            //init the stats containers and the text view
            //Basic stats container
            val basicStatsLayout = binding.layoutBasicStats
            val rank = binding.userStatsRank
            val challengesPlayed = binding.userStatsChallengesPlayed
            val challengesCreated = binding.userStatsChallengesCreated
            val points = binding.userStatsPoints
            val pointsEarned = binding.pointsEarned
            val pointsSpent = binding.pointsSpent
            val basicStatsTitleContainer = binding.basicStatsTitleContainer
            //Challenges stats container
            val challengesStatsContainer = binding.challengesStatsContainer
            val challengesStatsLayout = binding.layoutChallengesStats
            basicStatsLayout.isVisible = false
            challengesStatsLayout.isVisible = false
            // allows the close and open for view the data of this container
            basicStatsTitleContainer.setOnClickListener {
                basicStatsLayout.isVisible = !basicStatsLayout.isVisible
            }
            challengesStatsContainer.setOnClickListener {
                challengesStatsLayout.isVisible = !challengesStatsLayout.isVisible
            }
            if (userId.isNotEmpty()) {
                userStatsController.getUser(userId) { user ->
                    if (user != null) {
                        Log.i("TheUserIs", "the value is $user")
                        rank.text = rank.text.toString() +"  " + userRank.toString()
                        challengesPlayed.text = challengesPlayed.text.toString() + "  " + user.challengesPlayed.toString()
                        challengesCreated.text = challengesCreated.text.toString()  + "  " + user.challengesCreated.toString()
                        points.text = points.text.toString() + "  " + user.personalScore.toString()
                        pointsEarned.text = pointsEarned.text.toString() + "  " + user.pointsEarned.toString()
                        pointsSpent.text = pointsSpent.text.toString() + "  " + user.pointSpent.toString()


                    }
                }
            }

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            android.R.id.home -> { // handle the click event of the back button
//                finish()
//                return true
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
}