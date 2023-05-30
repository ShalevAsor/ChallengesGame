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

class FlagChallenge : AppCompatActivity() {
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

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide()

        setContentView(R.layout.activity_flag_challenge)
        challengeController = ChallengeController(this)

        scoreView = findViewById(R.id.scoreView)
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)
        button4 = findViewById(R.id.button4)
        buttons = arrayOf(button1, button2, button3, button4)
        imageView = findViewById<ImageView>(R.id.imageView)

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
        if(gameNum>7) {
            finishGame()
            return
        }

        var time_in_sec=10;
        timer = object: CountDownTimer(10000, 1000) {
            @SuppressLint("WrongViewCast")
            override fun onTick(millisUntilFinished: Long) {
                LOG.info("$time_in_sec sec")

                val timeView: TextView = findViewById(R.id.timeView) as TextView
                timeView.text="Time left: $time_in_sec"

                time_in_sec--
            }

            override fun onFinish() {
                gameNum++
                game()
            }
        }
        timer.start() // starting the timer

        var flagNum:Int
        var fakeFlagNum1:Int
        var fakeFlagNum2:Int
        var fakeFlagNum3:Int
        val originalButtonColor = button1.background

        val flags_list = ArrayList<Int>()
        val flag_arr = resources.obtainTypedArray(R.array.flags)

        (0 until flag_arr.length()).forEach {
            val flag = flag_arr.getResourceId(it, -1)
            flags_list.add(flag)
        }
        flag_arr.recycle()


        // getting a random flag number
        do {
            flagNum = (0 until flags_list.size).random()
            fakeFlagNum1 = (0 until flags_list.size).random()
            fakeFlagNum2 = (0 until flags_list.size).random()
            fakeFlagNum3 = (0 until flags_list.size).random()
        }while (flagNum==fakeFlagNum1 || flagNum==fakeFlagNum2 || flagNum==fakeFlagNum3)

        // setting the image flag on the screen
        imageView.setImageDrawable(ContextCompat.getDrawable(this, flags_list[flagNum]))

        // getting the right flag name
        imageView.setTag(flags_list[flagNum])
        var right_flag_name = resources.getResourceName(imageView.getTag() as Int)
        right_flag_name=right_flag_name.substring(right_flag_name.indexOf('/')+1).replace('_',' ')

        //getting the fake flag name 1
        imageView.setTag(flags_list[fakeFlagNum1])
        var fake_flag_name1 = resources.getResourceName(imageView.getTag() as Int)
        fake_flag_name1=fake_flag_name1.substring(fake_flag_name1.indexOf('/')+1).replace('_',' ')

        //getting the fake flag name 2
        imageView.setTag(flags_list[fakeFlagNum2])
        var fake_flag_name2 = resources.getResourceName(imageView.getTag() as Int)
        fake_flag_name2 = fake_flag_name2.substring(fake_flag_name2.indexOf('/')+1).replace('_',' ')

        //getting the fake flag name 3
        imageView.setTag(flags_list[fakeFlagNum3])
        var fake_flag_name3 = resources.getResourceName(imageView.getTag() as Int)
        fake_flag_name3=fake_flag_name3.substring(fake_flag_name3.indexOf('/')+1).replace('_',' ')

        val rightButton = (1..4).random() // getting a random number to place the right answer

        // setting the answers in button txt
        if (rightButton==1) {
            button1.text = right_flag_name
            button2.text = fake_flag_name1
            button3.text = fake_flag_name2
            button4.text = fake_flag_name3
        }
        else if (rightButton==2){
            button1.text = fake_flag_name1
            button2.text = right_flag_name
            button3.text = fake_flag_name2
            button4.text = fake_flag_name3
        }
        else if (rightButton==3){
            button1.text = fake_flag_name1
            button2.text = fake_flag_name2
            button3.text = right_flag_name
            button4.text = fake_flag_name3
        }
        else{
            button1.text = fake_flag_name1
            button2.text = fake_flag_name2
            button3.text = fake_flag_name3
            button4.text = right_flag_name
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

    private fun finishGame() {
        timer.cancel()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val markerId = intent.getStringExtra("MARKER_ID")
        val oldChTopScore = intent.getIntExtra("TOP_SCORES", 0)
        val oldUserTopScore = intent.getIntExtra("USER_TOP_SCORE",0)
        val challName = intent.getStringExtra("CHALL_NAME")
        if(markerId != null && userId != null ) {
            challengeController.updateAllScores(userId, markerId.toString(), counterForRightAnswers)
        }
        LOG.info("Time is up with $counterForRightAnswers points")
        val intent = Intent(this@FlagChallenge, MapsActivity::class.java)
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
        val timer_button = object : CountDownTimer(500, 100) {
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
        timer_button.start()
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