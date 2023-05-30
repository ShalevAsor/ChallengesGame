package com.geochamp.myapp.View
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.geochamp.myapp.Controller.ChallengeController
import com.geochamp.myapp.R
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import java.util.logging.Logger


class TapTheNumChallenge : AppCompatActivity() {
    private var counter=5
    private lateinit var buttons_list:List<Button>
    private val LOG = Logger.getLogger(this.javaClass.name)
    private lateinit var scoreView: TextView
    private lateinit var challengeController: ChallengeController
    private lateinit var timer: CountDownTimer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide()
        setContentView(R.layout.activity_tap_the_num_challenge)
        challengeController = ChallengeController(this)
        var time_in_sec=20;
        scoreView = findViewById(R.id.theScore)

        timer = object: CountDownTimer(20000, 1000) {
            @SuppressLint("WrongViewCast")
            override fun onTick(millisUntilFinished: Long) {
                println("$time_in_sec sec")

                val timeView: TextView = findViewById(R.id.theTime) as TextView
                timeView.text="Time left: $time_in_sec"

                time_in_sec--
            }

            override fun onFinish() {
                var counter_for_right_answers = counter-5
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val markerId = intent.getStringExtra("MARKER_ID")
                val oldChTopScore = intent.getIntExtra("TOP_SCORES", 0)
                val oldUserTopScore = intent.getIntExtra("USER_TOP_SCORE",0)
                val challName = intent.getStringExtra("CHALL_NAME")
                if(markerId != null && userId != null ) {
                    challengeController.updateAllScores(userId, markerId.toString(), counter_for_right_answers)
                }
                println("Time is up with $counter_for_right_answers points")
                val intent = Intent(this@TapTheNumChallenge, MapsActivity::class.java)
                intent.putExtra("SCORE",counter_for_right_answers)
                intent.putExtra("MARKER_ID",markerId)
                intent.putExtra("CHALL_NAME",challName)
                intent.putExtra("OLD_CH_TOP_SCORE",oldChTopScore)
                intent.putExtra("OLD_USER_TOP_SCORE",oldUserTopScore)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }
        }
        timer.start() // starting the timer

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        buttons_list = listOf(findViewById<View>(R.id.b1) as Button, findViewById<View>(R.id.b2) as Button, findViewById<View>(
            R.id.b3
        ) as Button, findViewById<View>(R.id.b4) as Button, findViewById<View>(R.id.b5) as Button)
        for (b in buttons_list) {
            change_button_to_random_location(displayMetrics, b)
        }

        val cancelButton = findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            timer.cancel()
            finish()
        }

    }

    override fun onBackPressed() {
        timer.cancel()
        finish()
    }

    // this method change
    fun change_button_to_random_location(displayMetrics:DisplayMetrics, button:Button) {
        runOnUiThread {
            val R = Random()
            var dx:Float
            var dy: Float

            do {
                dx = R.nextFloat() * (displayMetrics.widthPixels - 300)
                dy = R.nextFloat() * (displayMetrics.heightPixels - 500)
            } while (!is_far_enough(dx, dy))

            button.animate()
                .x(dx)
                .y(dy)
                .setDuration(0)
                .start()
        }

        button.setOnClickListener {
            if (isMinButton(button)) {
                counter++

                val score = counter-5
                scoreView.text="Score: $score"

                button.text = counter.toString()
                change_button_to_random_location(displayMetrics, button)
            }
            else{
                button.setBackgroundResource(R.drawable.rounded_corner_red_background)

                // create a timer to reset the button color
                val timer = object : CountDownTimer(300, 100) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        button.setBackgroundResource(R.drawable.rounded_corner_background)
                    }
                }
                timer.start()
            }
        }
    }

    fun is_far_enough(x:Float, y:Float): Boolean{
        for (b in buttons_list){
            var dist = Math.sqrt(
                Math.pow((b.x-x).toDouble(),2.0)
                        + Math.pow((b.y-y).toDouble(),2.0)
            )
            if (dist<400) return false
        }
        var text_list:List<TextView> = arrayListOf(findViewById<View>(R.id.theScore) as TextView, findViewById<View>(
            R.id.theTime
        ) as TextView)
        for (t in text_list){
            var dist = Math.sqrt(
                Math.pow((t.x-x).toDouble(),2.0)
                        + Math.pow((t.y-y).toDouble(),2.0)
            )
            if (dist<400) return false
        }
        return true
    }

    private fun isMinButton(button: Button): Boolean {
        var min_num:Int = button.text.toString().toInt()
        return min_num==(counter-4)
    }


}