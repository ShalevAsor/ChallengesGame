package com.geochamp.myapp.View

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.geochamp.myapp.R
import com.google.android.material.snackbar.Snackbar


class JumpActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null
    private var jumpCount = 0
    private var lastJumpTime: Long = 0
    private val jumpInterval = 600 // milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jump)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Check if the device has an accelerometer sensor
        if (sensor == null) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Your device does not have an accelerometer sensor.",
                Snackbar.LENGTH_INDEFINITE
            ).show()
        }

        val button = findViewById<Button>(R.id.cancel_button)
        button.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

    }

    override fun onResume() {
        super.onResume()

        // Register the accelerometer sensor listener
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()

        // Unregister the accelerometer sensor listener
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val accelerationX = it.values[0]
                val accelerationY = it.values[1]
                val accelerationZ = it.values[2]

                // Calculate the total acceleration
                val acceleration =
                    Math.sqrt((accelerationX * accelerationX + accelerationY * accelerationY + accelerationZ * accelerationZ).toDouble())

                // Check if the acceleration is above a certain threshold to detect a jump
                if (acceleration > 40) {
                    val now = System.currentTimeMillis()
                    if (now - lastJumpTime > jumpInterval) {
                        jumpCount++
                        lastJumpTime = now

                        // Update the UI to show the jump count
                        updateJumpCount()
                    }
                }
            }
        }
    }

    private fun updateJumpCount() {
        runOnUiThread {
            val jumpCountView = findViewById<TextView>(R.id.jump_count_view)
            jumpCountView.text = "Jumps: $jumpCount"

            if (jumpCount >= 20) {
                // Disable the sensor listener and finish the activity
                sensorManager.unregisterListener(this)
                val resultIntent = Intent()
                resultIntent.putExtra("result", "some result")
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}