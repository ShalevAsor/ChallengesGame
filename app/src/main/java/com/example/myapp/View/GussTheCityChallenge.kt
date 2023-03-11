package com.example.myapp.View

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapp.Controller.ChallengeController
import com.example.myapp.R
import com.google.firebase.auth.FirebaseAuth
import java.util.logging.Logger


class GussTheCityChallenge : AppCompatActivity() {
    private val LOG = Logger.getLogger(this.javaClass.name)

    private var counterForRightAnswers = 0

    private lateinit var scoreView: TextView
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var buttons: Array<Button>
    private lateinit var imageView: ImageView
    private lateinit var challengeController: ChallengeController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide action bar
        supportActionBar?.hide()

        // Set content view
        setContentView(R.layout.activity_guss_the_city_challenge)

        // Initialize variables
        challengeController = ChallengeController(this)
        scoreView = findViewById(R.id.thescoreView)
        button1 = findViewById(R.id.bu1)
        button2 = findViewById(R.id.bu2)
        button3 = findViewById(R.id.bu3)
        button4 = findViewById(R.id.bu4)
        buttons = arrayOf(button1, button2, button3, button4)
        imageView = findViewById<ImageView>(R.id.theimageView)

        // Set up timer
        val timer = object: CountDownTimer(15000, 1000) {

            var timeInSeconds = 15

            override fun onTick(millisUntilFinished: Long) {

                // Update time view
                val timeView: TextView = findViewById(R.id.thetimeView) as TextView
                timeView.text = "Time left: $timeInSeconds"

                timeInSeconds--
            }

            override fun onFinish() {

                // Update score in database
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val markerId = intent.getStringExtra("MARKER_ID")
                if(markerId != null && userId != null ) {
                    challengeController.updateAllScores(userId, markerId.toString(), counterForRightAnswers)
                }

                // Navigate to MapsActivity
                val intent = Intent(this@GussTheCityChallenge, MapsActivity::class.java)
                startActivity(intent)
            }
        }
        timer.start() // Start the timer

        // Start the game
        game()
    }

    override fun onBackPressed() {
        LOG.info("Can not go back in a middle of a challenge")
    }


    private fun game() {
        // Initialize variables
        val citiesList = ArrayList<Int>()
        val citiesArr = resources.obtainTypedArray(R.array.cities)

        // Populate citiesList with resource ids from the cities array
        for (i in 0 until citiesArr.length()) {
            val flag = citiesArr.getResourceId(i, -1)
            citiesList.add(flag)
        }

        citiesArr.recycle()

        // Generate unique random city numbers
        var cityNum: Int
        var fakeCityNum1: Int
        var fakeCityNum2: Int
        var fakeCityNum3: Int


        // getting a random city number
        do {
            cityNum = (0 until citiesList.size).random()
            fakeCityNum1 = (0 until citiesList.size).random()
            fakeCityNum2 = (0 until citiesList.size).random()
            fakeCityNum3 = (0 until citiesList.size).random()
        }while (cityNum==fakeCityNum1 || cityNum==fakeCityNum2 || cityNum==fakeCityNum3)
        // setting the image city on the screen
        imageView.setImageDrawable(ContextCompat.getDrawable(this, citiesList[cityNum]))

        // getting the right city name
        imageView.setTag(citiesList[cityNum])
        var right_city_name = resources.getResourceName(imageView.getTag() as Int)
        right_city_name=right_city_name.substring(right_city_name.indexOf('/')+1).replace('_',' ')

        //getting the fake city name 1
        imageView.setTag(citiesList[fakeCityNum1])
        var fake_city_name1 = resources.getResourceName(imageView.getTag() as Int)
        fake_city_name1=fake_city_name1.substring(fake_city_name1.indexOf('/')+1).replace('_',' ')

        //getting the fake city name 2
        imageView.setTag(citiesList[fakeCityNum2])
        var fake_city_name2 = resources.getResourceName(imageView.getTag() as Int)
        fake_city_name2 = fake_city_name2.substring(fake_city_name2.indexOf('/')+1).replace('_',' ')

        //getting the fake city name 3
        imageView.setTag(citiesList[fakeCityNum3])
        var fake_city_name3 = resources.getResourceName(imageView.getTag() as Int)
        fake_city_name3=fake_city_name3.substring(fake_city_name3.indexOf('/')+1).replace('_',' ')


        val rightButton = (1..4).random() // getting a random number to place the right answer

        // setting the answers in button txt
        if (rightButton==1) {
            button1.text = right_city_name
            button2.text = fake_city_name1
            button3.text = fake_city_name2
            button4.text = fake_city_name3
        }
        else if (rightButton==2){
            button1.text = fake_city_name1
            button2.text = right_city_name
            button3.text = fake_city_name2
            button4.text = fake_city_name3
        }
        else if (rightButton==3){
            button1.text = fake_city_name1
            button2.text = fake_city_name2
            button3.text = right_city_name
            button4.text = fake_city_name3
        }
        else{
            button1.text = fake_city_name1
            button2.text = fake_city_name2
            button3.text = fake_city_name3
            button4.text = right_city_name
        }

        // validating the answer and starting new game
        button1.setOnClickListener {
            handleAnswer(rightButton, 1)
        }

        button2.setOnClickListener {
            handleAnswer(rightButton, 2)
        }

        button3.setOnClickListener {
            handleAnswer(rightButton, 3)
        }

        button4.setOnClickListener {
            handleAnswer(rightButton, 4)
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
        rightButton: Int,
        currButtonNum: Int
    ) {
        disableButtons() // disable the buttons to prevent multiple clicks
        val originalButtonColor = button1.background // get the original color of the clicked button

        // check if the user's answer is correct
        if (currButtonNum == rightButton) {
            counterForRightAnswers++
        }
        changeButtonsColor(rightButton)

        // create a timer to reset the buttons and start a new game
        val timer = object : CountDownTimer(500, 100) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                for (b in buttons) {
                    b.background = originalButtonColor
                }
                enableButtons() // enable the buttons for the next game

                scoreView.text = "Score: $counterForRightAnswers" // update the score text
                game() // start a new game
            }
        }
        timer.start()
    }

    private fun changeButtonsColor(rightButton: Int) {
        for (i in 0 until buttons.size){
            if (i!=rightButton-1) {
                buttons[i].setBackgroundResource(R.drawable.rounded_corner_red_background)
            } else buttons[i].setBackgroundResource(R.drawable.rounded_corner_green_background)
        }
    }


    private fun disableButtons() {
        button1.isEnabled = false
        button2.isEnabled = false
        button3.isEnabled = false
        button4.isEnabled = false
    }

    private fun enableButtons() {
        button1.isEnabled = true
        button2.isEnabled = true
        button3.isEnabled = true
        button4.isEnabled = true
    }

}