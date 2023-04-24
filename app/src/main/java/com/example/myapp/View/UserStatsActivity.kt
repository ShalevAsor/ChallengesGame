package com.example.myapp.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import com.example.myapp.R
import com.example.myapp.databinding.ActivityUserStatsBinding

class UserStatsActivity : AppCompatActivity() {

    private lateinit var binding:ActivityUserStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Challenges Game"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //init the stats containers and the text view
        //Basic stats container
        val basicStatsLayout = binding.layoutBasicStats
        val rank = binding.userStatsRank
        val challengesPlayed = binding.userStatsChallengesPlayed
        val timePlayed = binding.userStatsTimePlayed
        val challengesCreated = binding.userStatsChallengesCreated
        val points = binding.userStatsPoints
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