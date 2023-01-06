package com.example.myapp


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import java.util.logging.Logger

class MathChallenge : AppCompatActivity() {
    private var counter_for_right_answers=0
    private val LOG = Logger.getLogger(this.javaClass.name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide()
        setContentView(R.layout.activity_math_challenge)

        var time_in_sec=15;
        val timer = object: CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                println("$time_in_sec sec")

                val timeView: TextView = findViewById(R.id.timetView3) as TextView
                timeView.text="Time left: $time_in_sec"

                time_in_sec--
            }

            override fun onFinish() {
                println("Time is up with $counter_for_right_answers points")
                val intent = Intent(this@MathChallenge, MapsActivity::class.java)
                startActivity(intent)
            }
        }
        timer.start() // starting the timer

        game() // starting the game
    }

    override fun onBackPressed() {
        LOG.info("Can not go back in a middle of a challenge")
    }

    private fun game() {
        var operation_list = listOf('-','+')
        var ans:Int
        var question:String
        val button_1: Button = findViewById(R.id.button1)
        val button_2: Button = findViewById(R.id.button2)

        // getting 2 random numbers
        var num1 = (0..500).random()
        var num2 = (0..500).random()
        val operationValue = operation_list.random()

        // checking what operation to do
        if (operationValue=='-'){
            ans = num1 - num2
            question = "$num1 - $num2"
        }
        else{
            ans = num1 + num2
            question = "$num1 + $num2"
        }
        var fake_ans = (ans-100..ans+100).random()

        // setting the question on the screen
        val textView = findViewById<TextView>(R.id.questionTxt)
        textView.setText(question).toString()

        val right_button = (1..2).random() // getting a random number to place the right answer

        // setting the answers in button txt
        if (right_button==1) {
            button_1.text = ans.toString()
            button_2.text = fake_ans.toString()
        }
        else{
            button_1.text = fake_ans.toString()
            button_2.text = ans.toString()
        }

        // validating the answer and starting new game
        button_1.setOnClickListener {
            if (right_button==1) counter_for_right_answers++
            println(counter_for_right_answers)

            val scoreView: TextView = findViewById(R.id.scoreView3) as TextView
            scoreView.text="Score: $counter_for_right_answers"

            game()
        }

        button_2.setOnClickListener {
            if (right_button==2) counter_for_right_answers++
            println(counter_for_right_answers)

            val scoreView: TextView = findViewById(R.id.scoreView3) as TextView
            scoreView.text="Score: $counter_for_right_answers"

            game()
        }
    }
}