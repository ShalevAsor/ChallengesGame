package com.example.myapp.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.myapp.Controller.TopScoresController
import com.example.myapp.Controller.UserProfileController
import com.example.myapp.Model.ScoreModel
import com.example.myapp.R
import com.example.myapp.databinding.ActivityUserStatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserStatsActivity : AppCompatActivity() {

    private lateinit var binding:ActivityUserStatsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var userId: String
    private lateinit var userProfileController: UserProfileController

    //user Rank
    private lateinit var topScoresController: TopScoresController
    private lateinit var topScores: MutableList<ScoreModel>
    var firstName: String? = null
    var totalScore: Long? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Challenges Game"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        firebaseAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("Markers")
        userId = firebaseAuth.currentUser!!.uid



        //Get user Rank
        if (userId != null) {
            topScoresController.getUser(userId){user ->
                if (user != null) {
                    firstName  = user.firstName
                    totalScore = user.personalScore
                    //Log.d("signgs", "the value is - $totalScore.")
                }
            }
        }
        topScoresController.getRank(topScores)
        val filteredList=topScores.filter{ it.firstName == firstName && it.total_score == totalScore }
        if (filteredList.isEmpty()) {
            Log.d("errorRank", "no such player was found")
        }
        val foundScoreModel = filteredList.first()
        val userRank:Int= topScores.indexOf(foundScoreModel) + 1


        //init the stats containers and the text view
        //Basic stats container
        val basicStatsLayout = binding.layoutBasicStats
        val rank = binding.userStatsRank
        val challengesPlayed = binding.userStatsChallengesPlayed
        val timePlayed = binding.userStatsTimePlayed
        val challengesCreated = binding.userStatsChallengesCreated
        val points = binding.userStatsPoints
        val pointsEarned=binding.pointsEarned
        val pointsSpent=binding.pointsSpent
        val basicStatsTitleContainer = binding.basicStatsTitleContainer
        //Challenges stats container
        val challengesStatsContainer = binding.challengesStatsContainer
        val challengesStatsLayout = binding.layoutChallengesStats
        basicStatsLayout.isVisible = false
        challengesStatsLayout.isVisible = false
        // allows the close and open for view the data of this container
        basicStatsTitleContainer.setOnClickListener{
            basicStatsLayout.isVisible = !basicStatsLayout.isVisible
        }
        challengesStatsContainer.setOnClickListener{
            challengesStatsLayout.isVisible = !challengesStatsLayout.isVisible
        }
        if(userId.isNotEmpty()){
            userProfileController.getUser(userId){ user ->
                if(user != null){
                    Log.i("TheUserIs", "the value is $user")
                    rank.text = userRank.toString()
                    challengesPlayed.text = user.challengesPlayed.toString()
                    //timePlayed.text = user.timePlayed.toString()
                    challengesCreated.text=user.challengesCreated.toString()
                    points.text=user.personalScore.toString()
                    pointsEarned.text=user.pointsEarned.toString()
                    pointsSpent.text=user.pointSpent.toString()


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