package com.example.myapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.logging.Logger

class FlagChallenge : AppCompatActivity() {
    private var counter_for_right_answers=0
    private val LOG = Logger.getLogger(this.javaClass.name)
    private lateinit var scoreView: TextView
    private lateinit var button_1: Button
    private lateinit var button_2: Button
    private lateinit var button_3: Button
    private lateinit var button_4: Button
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide()
        setContentView(R.layout.activity_flag_challenge)

        scoreView = findViewById(R.id.scoreView)
        button_1 = findViewById(R.id.button1)
        button_2 = findViewById(R.id.button2)
        button_3 = findViewById(R.id.button3)
        button_4 = findViewById(R.id.button4)
        imageView = findViewById<ImageView>(R.id.imageView)

        var time_in_sec=15;
        val timer = object: CountDownTimer(15000, 1000) {
            @SuppressLint("WrongViewCast")
            override fun onTick(millisUntilFinished: Long) {
                println("$time_in_sec sec")

                val timeView: TextView = findViewById(R.id.timeView) as TextView
                timeView.text="Time left: $time_in_sec"

                time_in_sec--
            }

            override fun onFinish() {
                println("Time is up with $counter_for_right_answers points")
                val intent = Intent(this@FlagChallenge, MapsActivity::class.java)
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
        val flags_list = ArrayList<Int>()
        val flag_arr = resources.obtainTypedArray(R.array.flags)

        (0 until flag_arr.length()).forEach {
            val flag = flag_arr.getResourceId(it, -1)
            flags_list.add(flag)
        }
        flag_arr.recycle()

        // getting a random flag number
        val random_flag_num = (0..flags_list.size-1).random()
        val random_of_fake_flag_num1 = (0..flags_list.size-1).random()
        val random_of_fake_flag_num2 = (0..flags_list.size-1).random()
        val random_of_fake_flag_num3 = (0..flags_list.size-1).random()

        // setting the image flag on the screen
        imageView.setImageDrawable(ContextCompat.getDrawable(this, flags_list[random_flag_num]))

        // getting the right flag name
        imageView.setTag(flags_list[random_flag_num])
        var right_flag_name = resources.getResourceName(imageView.getTag() as Int)
        right_flag_name=right_flag_name.substring(right_flag_name.indexOf('/')+1).replace('_',' ')

        //getting the fake flag name 1
        imageView.setTag(flags_list[random_of_fake_flag_num1])
        var fake_flag_name1 = resources.getResourceName(imageView.getTag() as Int)
        fake_flag_name1=fake_flag_name1.substring(fake_flag_name1.indexOf('/')+1).replace('_',' ')

        //getting the fake flag name 2
        imageView.setTag(flags_list[random_of_fake_flag_num2])
        var fake_flag_name2 = resources.getResourceName(imageView.getTag() as Int)
        fake_flag_name2 = fake_flag_name2.substring(fake_flag_name2.indexOf('/')+1).replace('_',' ')

        //getting the fake flag name 3
        imageView.setTag(flags_list[random_of_fake_flag_num3])
        var fake_flag_name3 = resources.getResourceName(imageView.getTag() as Int)
        fake_flag_name3=fake_flag_name3.substring(fake_flag_name3.indexOf('/')+1).replace('_',' ')

        val right_button = (1..4).random() // getting a random number to place the right answer

        // setting the answers in button txt
        if (right_button==1) {
            button_1.text = right_flag_name
            button_2.text = fake_flag_name1
            button_3.text = fake_flag_name2
            button_4.text = fake_flag_name3
        }
        else if (right_button==2){
            button_1.text = fake_flag_name1
            button_2.text = right_flag_name
            button_3.text = fake_flag_name2
            button_4.text = fake_flag_name3
        }
        else if (right_button==3){
            button_1.text = fake_flag_name1
            button_2.text = fake_flag_name2
            button_3.text = right_flag_name
            button_4.text = fake_flag_name3
        }
        else{
            button_1.text = fake_flag_name1
            button_2.text = fake_flag_name2
            button_3.text = fake_flag_name3
            button_4.text = right_flag_name
        }

        // validating the answer and starting new game
        button_1.setOnClickListener {
            if (right_button==1) counter_for_right_answers++
            println(counter_for_right_answers)
            scoreView.text="Score: $counter_for_right_answers"
            game()
        }

        button_2.setOnClickListener {
            if (right_button==2) counter_for_right_answers++
            println(counter_for_right_answers)
            scoreView.text="Score: $counter_for_right_answers"
            game()
        }

        button_3.setOnClickListener {
            if (right_button==3) counter_for_right_answers++
            println(counter_for_right_answers)
            scoreView.text="Score: $counter_for_right_answers"
            game()
        }

        button_4.setOnClickListener {
            if (right_button==4) counter_for_right_answers++
            println(counter_for_right_answers)
            scoreView.text="Score: $counter_for_right_answers"
            game()
        }
    }

}