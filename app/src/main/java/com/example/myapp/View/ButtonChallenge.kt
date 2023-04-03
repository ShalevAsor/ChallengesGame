package com.example.myapp.View


import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import com.example.myapp.Controller.ChallengeController
import com.example.myapp.R
import com.google.firebase.auth.FirebaseAuth
import java.util.logging.Logger

class ButtonChallenge : AppCompatActivity() {
    private var counter = 0
    private lateinit var scoreView: TextView
    private val LOG = Logger.getLogger(this.javaClass.name)
    private lateinit var challengeController: ChallengeController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button_challenge)

        scoreView = findViewById(R.id.scoreView) as TextView
        challengeController = ChallengeController(this)

        var time_in_sec=10;
        val timer = object: CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                println("$time_in_sec sec")

                val timeView: TextView = findViewById(R.id.timeView) as TextView
                timeView.text="Time left: $time_in_sec"

                time_in_sec--
            }

            override fun onFinish() {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val markerId = intent.getStringExtra("MARKER_ID")
                val oldChTopScore = intent.getIntExtra("TOP_SCORES", 0)
                val oldUserTopScore = intent.getIntExtra("USER_TOP_SCORE",0)
                val challName = intent.getStringExtra("CHALL_NAME")
                if(markerId != null && userId != null ) {
                    challengeController.updateAllScores(userId, markerId.toString(), counter)
                    challengeController.updateBillboard(userId,counter)
                }

                println("Time is up with $counter points")
                val intent = Intent(this@ButtonChallenge, MapsActivity::class.java)
                intent.putExtra("SCORE",counter)
                intent.putExtra("MARKER_ID",markerId)
                intent.putExtra("CHALL_NAME",challName)
                intent.putExtra("OLD_CH_TOP_SCORE",oldChTopScore)
                intent.putExtra("OLD_USER_TOP_SCORE",oldUserTopScore)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }
        }
        timer.start() // starting the timer

    }

    override fun onBackPressed() {
        LOG.info("Can not go back in a middle of a challenge")
    }

    fun clickCounter(view: View) {
        counter++
        scoreView.text="Score: $counter"
    }
}