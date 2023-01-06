package com.example.myapp
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import java.util.logging.Logger


class TapTheNumChallenge : AppCompatActivity() {
    private var counter=5
    private lateinit var buttons_list:List<Button>
    private val LOG = Logger.getLogger(this.javaClass.name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide()
        setContentView(R.layout.activity_tap_the_num_challenge)

        var time_in_sec=20;
        val timer = object: CountDownTimer(20000, 1000) {
            @SuppressLint("WrongViewCast")
            override fun onTick(millisUntilFinished: Long) {
                println("$time_in_sec sec")

                val timeView: TextView = findViewById(R.id.theTime) as TextView
                timeView.text="Time left: $time_in_sec"

                val scoreView: TextView = findViewById(R.id.theScore) as TextView
                var counter_for_right_answers = counter-5
                scoreView.text="Score: $counter_for_right_answers"

                time_in_sec--
            }

            override fun onFinish() {
                var counter_for_right_answers = counter-5
                println("Time is up with $counter_for_right_answers points")
                val intent = Intent(this@TapTheNumChallenge, MapsActivity::class.java)
                startActivity(intent)
            }
        }
        timer.start() // starting the timer

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        buttons_list = listOf(findViewById<View>(R.id.b1) as Button, findViewById<View>(R.id.b2) as Button, findViewById<View>(R.id.b3) as Button, findViewById<View>(R.id.b4) as Button, findViewById<View>(R.id.b5) as Button)
        for (b in buttons_list) {
            change_button_to_random_location(displayMetrics, b)
        }
    }

    override fun onBackPressed() {
        LOG.info("Can not go back in a middle of a challenge")
    }

    // this method change
    fun change_button_to_random_location(displayMetrics:DisplayMetrics, button:Button) {
        runOnUiThread {
            val R = Random()
            var dx:Float
            var dy: Float

            do {
                dx = R.nextFloat() * (displayMetrics.widthPixels - 300)
                dy = R.nextFloat() * (displayMetrics.heightPixels - 300)
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
                button.text = counter.toString()
                change_button_to_random_location(displayMetrics, button)
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
        var text_list:List<TextView> = arrayListOf(findViewById<View>(R.id.theScore) as TextView, findViewById<View>(R.id.theTime) as TextView)
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
        for (b in buttons_list){
            var num=b.text.toString().toInt()
            if (num<min_num) return false
        }
        return true
    }


}