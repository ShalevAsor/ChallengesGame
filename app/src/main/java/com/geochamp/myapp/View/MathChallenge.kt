package com.geochamp.myapp.View

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import com.geochamp.myapp.Controller.ChallengeController
import com.geochamp.myapp.R
import com.google.firebase.auth.FirebaseAuth
import java.util.logging.Logger

class MathChallenge : AppCompatActivity() {
    private var counterForRightAnswers = 0
    private val LOG = Logger.getLogger(this.javaClass.name)
    private lateinit var challengeController: ChallengeController
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the action bar
        supportActionBar?.hide()

        // Set the layout for this activity
        setContentView(R.layout.activity_math_challenge)

        // Initialize the challenge controller
        challengeController = ChallengeController(this)

        // Set the time for the challenge (in seconds)
        var time_in_sec = 15

        // Create the countdown timer
        timer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                LOG.info("$time_in_sec sec")

                // Update the time left text view
                val timeView: TextView = findViewById(R.id.timetView3)
                timeView.text = "Time left: $time_in_sec"

                // Decrement the time left
                time_in_sec--
            }

            override fun onFinish() {
                // When the timer is finished, update the scores in the database and start the MapsActivity
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val markerId = intent.getStringExtra("MARKER_ID")
                val oldChTopScore = intent.getIntExtra("TOP_SCORES", 0)
                val oldUserTopScore = intent.getIntExtra("USER_TOP_SCORE",0)
                val challName = intent.getStringExtra("CHALL_NAME")
                if (markerId != null && userId != null) {
                    challengeController.updateAllScores(
                        userId,
                        markerId.toString(),
                        counterForRightAnswers
                    )
                }

                LOG.info("Time is up with $counterForRightAnswers points")

                // Start the MapsActivity
                val intent = Intent(this@MathChallenge, MapsActivity::class.java)
                intent.putExtra("SCORE",counterForRightAnswers)
                intent.putExtra("MARKER_ID",markerId)
                intent.putExtra("CHALL_NAME",challName)
                intent.putExtra("OLD_CH_TOP_SCORE",oldChTopScore)
                intent.putExtra("OLD_USER_TOP_SCORE",oldUserTopScore)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }
        }

        // Start the countdown timer
        timer.start()

        val cancelButton = findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            timer.cancel()
            finish()
        }

        // Start the game
        game()
    }

    override fun onBackPressed() {
        timer.cancel()
        finish()
    }

    private fun game() {
        // Define the possible operations
        val operation_list = listOf('-', '+')

        // Variables to hold the question and answer
        var ans: Int
        var question: String

        // Get references to the buttons and score text view
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)

        // Get two random numbers
        var num1 = (0..500).random()
        var num2 = (0..500).random()

        // Choose a random operation
        val operationValue = operation_list.random()

        // Set the question and answer based on the chosen operation
        if (operationValue == '-') {
            ans = num1 - num2
            question = "$num1 - $num2"
        } else {
            ans = num1 + num2
            question = "$num1 + $num2"
        }
        var fakeAns = (ans - 100..ans + 100).random()

        // setting the question on the screen
        val questionTextView = findViewById<TextView>(R.id.questionTxt)
        questionTextView.text = question

        val rightButton = (1..2).random() // getting a random number to place the right answer

        // set the answers in the buttons
        if (rightButton == 1) {
            button1.text = ans.toString()
            button2.text = fakeAns.toString()
        } else {
            button1.text = fakeAns.toString()
            button2.text = ans.toString()
        }

        // set the onClickListener for button1
        button1.setOnClickListener {
            handleAnswer(button1, button2, rightButton, 1)
        }

        // set the onClickListener for button2
        button2.setOnClickListener {
            handleAnswer(button2, button1, rightButton, 2)
        }
    }

    /**
     * A helper function to handle the user's answer.
     *
     * @param userButton the button clicked by the user
     * @param otherButton the other button not clicked by the user
     * @param rightButton the button with the right answer
     * @param rightAnswer the right answer to the question
     */
    private fun handleAnswer(
        userButton: Button,
        otherButton: Button,
        rightButton: Int,
        currButtonNum: Int
    ) {
        disableButtons() // disable the buttons to prevent multiple clicks
        val originalButtonColor =
            userButton.background // get the original color of the clicked button

        // check if the user's answer is correct
        if (currButtonNum == rightButton) {
            userButton.setBackgroundResource(R.drawable.rounded_corner_green_background)
            counterForRightAnswers++
        } else {
            userButton.setBackgroundResource(R.drawable.rounded_corner_red_background)
        }

        // create a timer to reset the buttons and start a new game
        val timer = object : CountDownTimer(500, 100) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                userButton.background =
                    originalButtonColor // reset the background color of the user's button
                otherButton.background =
                    originalButtonColor // reset the background color of the other button
                enableButtons() // enable the buttons for the next game
                val scoreTextView = findViewById<TextView>(R.id.scoreView3)
                scoreTextView.text = "Score: $counterForRightAnswers" // update the score text
                game() // start a new game
            }
        }
        timer.start()
    }

    /**
     * A helper function to disable the answer buttons.
     */
    private fun disableButtons() {
        button1.isEnabled = false
        button2.isEnabled = false
    }

    /**
     * A helper function to enable the answer buttons.
     */
    private fun enableButtons() {
        button1.isEnabled = true
        button2.isEnabled = true
    }
}