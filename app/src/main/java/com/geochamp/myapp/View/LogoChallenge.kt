package com.geochamp.myapp.View

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.geochamp.myapp.Controller.ChallengeController
import com.geochamp.myapp.R
import com.google.firebase.auth.FirebaseAuth
import java.util.logging.Logger

class LogoChallenge : AppCompatActivity() {

    private var counterForRightAnswers=0
    private var gameNum=0
    private val LOG = Logger.getLogger(this.javaClass.name)
    private lateinit var scoreView: TextView
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var buttons: Array<Button>
    private lateinit var imageView: ImageView
    private lateinit var challengeController: ChallengeController
    private lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide()

        setContentView(R.layout.activity_logo_challenge)

        challengeController = ChallengeController(this)
        scoreView = findViewById(R.id.thescoreView)
        button1 = findViewById(R.id.bu1)
        button2 = findViewById(R.id.bu2)
        button3 = findViewById(R.id.bu3)
        button4 = findViewById(R.id.bu4)
        buttons = arrayOf(button1, button2, button3, button4)
        imageView = findViewById<ImageView>(R.id.theimageView)

        val cancelButton = findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            timer.cancel()
            finish()
        }

        game() // starting the game
    }

    override fun onBackPressed() {
        timer.cancel()
        finish()
    }

    private fun game() {
        if (gameNum>7){
            end_game()
            return
        }

        var time_in_sec=10;
        timer = object: CountDownTimer(10000, 1000) {
            @SuppressLint("WrongViewCast")
            override fun onTick(millisUntilFinished: Long) {
                println("$time_in_sec sec")

                val timeView: TextView = findViewById(R.id.thetimeView) as TextView
                timeView.text="Time left: $time_in_sec"

                time_in_sec--
            }

            override fun onFinish() {
                gameNum++
                game()
            }
        }
        timer.start() // starting the timer

        var logoNum:Int
        var fakeLogoNum1:Int
        var fakeLogoNum2:Int
        var fakeLogoNum3:Int

        val logo_list = ArrayList<Int>()
        val logo_arr = resources.obtainTypedArray(R.array.logos)
        (0 until logo_arr.length()).forEach {
            val flag = logo_arr.getResourceId(it, -1)
            logo_list.add(flag)
        }
        logo_arr.recycle()

        // getting a random logo number
        do {
            logoNum = (0 until logo_list.size).random()
            fakeLogoNum1 = (0 until logo_list.size).random()
            fakeLogoNum2 = (0 until logo_list.size).random()
            fakeLogoNum3 = (0 until logo_list.size).random()
        }while (logoNum==fakeLogoNum1 || logoNum==fakeLogoNum2 || logoNum==fakeLogoNum3)

        // setting the image logo on the screen
        imageView.setImageDrawable(ContextCompat.getDrawable(this, logo_list[logoNum]))

        // getting the right logo name
        imageView.setTag(logo_list[logoNum])
        var right_logo_name = resources.getResourceName(imageView.getTag() as Int)
        right_logo_name=right_logo_name.substring(right_logo_name.indexOf('/')+1).replace('_',' ')

        //getting the fake logo name 1
        imageView.setTag(logo_list[fakeLogoNum1])
        var fake_logo_name1 = resources.getResourceName(imageView.getTag() as Int)
        fake_logo_name1=fake_logo_name1.substring(fake_logo_name1.indexOf('/')+1).replace('_',' ')

        //getting the fake logo name 2
        imageView.setTag(logo_list[fakeLogoNum2])
        var fake_logo_name2 = resources.getResourceName(imageView.getTag() as Int)
        fake_logo_name2 = fake_logo_name2.substring(fake_logo_name2.indexOf('/')+1).replace('_',' ')

        //getting the fake logo name 3
        imageView.setTag(logo_list[fakeLogoNum3])
        var fake_logo_name3 = resources.getResourceName(imageView.getTag() as Int)
        fake_logo_name3=fake_logo_name3.substring(fake_logo_name3.indexOf('/')+1).replace('_',' ')


        val rightButton = (1..4).random() // getting a random number to place the right answer

        // setting the answers in button txt
        if (rightButton==1) {
            button1.text = right_logo_name
            button2.text = fake_logo_name1
            button3.text = fake_logo_name2
            button4.text = fake_logo_name3
        }
        else if (rightButton==2){
            button1.text = fake_logo_name1
            button2.text = right_logo_name
            button3.text = fake_logo_name2
            button4.text = fake_logo_name3
        }
        else if (rightButton==3){
            button1.text = fake_logo_name1
            button2.text = fake_logo_name2
            button3.text = right_logo_name
            button4.text = fake_logo_name3
        }
        else{
            button1.text = fake_logo_name1
            button2.text = fake_logo_name2
            button3.text = fake_logo_name3
            button4.text = right_logo_name
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

    private fun end_game() {
        timer.cancel()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val markerId = intent.getStringExtra("MARKER_ID")
        val oldChTopScore = intent.getIntExtra("TOP_SCORES", 0)
        val oldUserTopScore = intent.getIntExtra("USER_TOP_SCORE",0)
        val challName = intent.getStringExtra("CHALL_NAME")
        if(markerId != null && userId != null ) {
            challengeController.updateAllScores(userId, markerId.toString(), counterForRightAnswers)
        }
        println("Time is up with $counterForRightAnswers points")
        val intent = Intent(this@LogoChallenge, MapsActivity::class.java)
        intent.putExtra("SCORE",counterForRightAnswers)
        intent.putExtra("MARKER_ID",markerId)
        intent.putExtra("CHALL_NAME",challName)
        intent.putExtra("OLD_CH_TOP_SCORE",oldChTopScore)
        intent.putExtra("OLD_USER_TOP_SCORE",oldUserTopScore)
        setResult(Activity.RESULT_OK,intent)
        finish()
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
        gameNum++
        timer.cancel()
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