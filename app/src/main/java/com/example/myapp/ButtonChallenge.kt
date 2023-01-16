package com.example.myapp


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.TextView
import java.util.logging.Logger

class ButtonChallenge : AppCompatActivity() {
    private var counter = 0
    private lateinit var scoreView: TextView
    private val LOG = Logger.getLogger(this.javaClass.name)
    private lateinit var updateData: UpdateData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button_challenge)

        scoreView = findViewById(R.id.scoreView) as TextView
        updateData = UpdateData()
        var time_in_sec=10;
        val timer = object: CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                println("$time_in_sec sec")

                val timeView: TextView = findViewById(R.id.timeView) as TextView
                timeView.text="Time left: $time_in_sec"

                time_in_sec--
            }

            override fun onFinish() {
                updateData.addToTotalScoreBillboard(counter)
                val markerID = intent.extras?.getString("markerID")
                val markerTopScore = intent.extras?.getInt("markerTopScore")
                Log.i("markerID",markerID as String)
                println("Time is up with $counter points")
                val intent = Intent(this@ButtonChallenge, MapsActivity::class.java)
                startActivity(intent)
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