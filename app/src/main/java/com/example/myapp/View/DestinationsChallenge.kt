package com.example.myapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapp.Controller.ChallengeController
import com.example.myapp.View.JumpActivity
import com.example.myapp.View.MapsActivity
import com.example.myapp.databinding.ActivityDestinationsChallengeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DestinationsChallenge : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDestinationsChallengeBinding
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var markers = arrayListOf<Marker>()
    private var score = 0
    private var timer: CountDownTimer? = null
    private var timeLeft = 0L
    private lateinit var timerTextView: TextView
    private lateinit var scoreView: TextView
    private lateinit var challengeController: ChallengeController
    private var currMarker = 0
    private lateinit var specialMarker: Marker
    private var hasDoublePoints = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide()
        binding = ActivityDestinationsChallengeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        challengeController = ChallengeController(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize the location manager and location listener
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        timerTextView = findViewById(R.id.timer_text_view)
        scoreView = findViewById(R.id.score_view)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Start the timer
        timeLeft = 300000
        startTimer()

        // Enable the user's location on the map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            locationListener?.let {
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f,
                    it
                )
            }
            // Focus the map on the user's location
            val location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                val userLocation = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18f))

                val checkPointBitmap = BitmapDescriptorFactory.fromResource(R.drawable.checkpointmarker)

                // Add the predetermined points to the points array
                if (markers.size==0) {
                    for (i in 1..3) {
                        var point: LatLng? = getLocation(location.latitude, location.longitude, 200)
                        var theMarker: Marker? =
                            point?.let { MarkerOptions().position(it).icon(checkPointBitmap).title("Point") }
                                ?.let { mMap.addMarker(it) }

                        if (theMarker != null) {
                            val circleOptions = CircleOptions()
                                .center(theMarker.position)
                                .radius(50.0)
                                .strokeWidth(2f)
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.argb(50, 0, 0, 255))

                            markers.add(theMarker)
                            mMap.addCircle(circleOptions)
                        }
                    }
                }
            }

            // Randomly select a position between point1 and point2
            val lat = getRandomNumber(markers[0].position.latitude, markers[1].position.latitude)
            val lng = getRandomNumber(markers[0].position.longitude, markers[1].position.longitude)
            var newPosition = LatLng(lat, lng)

            // Create a new marker with the double point symbol
            val doublePointBitmap = BitmapDescriptorFactory.fromResource(R.drawable.storm_marker)
            val newMarkerOptions = MarkerOptions()
                .position(newPosition)
                .icon(doublePointBitmap)
                .title("New Marker")

            specialMarker = mMap.addMarker(newMarkerOptions)!!



            //adding listener to the markers
            mMap.setOnMarkerClickListener { marker ->
                val location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    currMarker = getMarkerNum(marker)

                    val markerLocation = Location("")
                    markerLocation.latitude = marker.position.latitude
                    markerLocation.longitude = marker.position.longitude

                    val distance = location.distanceTo(markerLocation)
                    if (distance > 50) {
                        Toast.makeText(
                            this,
                            "You are too far from the Marker",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else { // the user next to the marker

                        if (marker == specialMarker) handleSpecialMarker(marker)
                        else{
                        val dialog = AlertDialog.Builder(this)
                        val dialogView = layoutInflater.inflate(R.layout.custom_dialog, null)

                        dialog.setView(dialogView)
                        val customDialog = dialog.create()
                        customDialog.show()

                        dialogView.findViewById<Button>(R.id.start_button).setOnClickListener {
                            customDialog.dismiss()

                            val intent =
                                Intent(this@DestinationsChallenge, JumpActivity::class.java)
                            jumpActivityResult.launch(intent)
                        }
                    }
                }
                }
                true
            }
        }
    }

    private fun handleSpecialMarker(marker: Marker) {
        val randomSpecialMarkerNum = Random.nextInt(0, 2)
        if (randomSpecialMarkerNum==0){
            Toast.makeText(
                this,
                "You Got Double Points",
                Toast.LENGTH_SHORT
            ).show()
            hasDoublePoints = 1
        }
        else{
            Toast.makeText(
                this,
                "You got extra 1 minute",
                Toast.LENGTH_SHORT
            ).show()
            timeLeft += TimeUnit.MINUTES.toMillis(1)
            timer?.cancel()
            startTimer()
        }
        marker.remove()
    }

    fun getRandomNumber(min: Double, max: Double): Double {
        return Random.nextDouble() * (max - min) + min
    }

    private fun getMarkerNum(marker: Marker): Int {
        for (i in 0 until markers.size){
            val m = markers[i]
            if(m==marker) return i
        }
        return -1
    }

    private val jumpActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val marker = markers[currMarker]
            markers.remove(marker)
            marker.remove()
            score+=30 + 30 * hasDoublePoints
            scoreView.text="Score: $score"

            if (markers.size==0){ // finishing the game
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val markerId = intent.getStringExtra("MARKER_ID")
                if(markerId != null && userId != null ) {
                    challengeController.updateAllScores(userId, markerId.toString(), score)
                }
                val intent = Intent(this@DestinationsChallenge, MapsActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeft) - minutes * 60
                updateTimer("$minutes:$seconds")
            }
            override fun onFinish() {
                updateTimer("Time's up!")
                Toast.makeText(this@DestinationsChallenge, "Time's up!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@DestinationsChallenge, MapsActivity::class.java)
                startActivity(intent)
            }
        }
        timer?.start()
    }

    private fun updateTimer(time: String) {
        timerTextView.text = "Timer: $time"
    }

    override fun onDestroy() {
        super.onDestroy()
        locationListener?.let { locationManager?.removeUpdates(it) }
        timer?.cancel()

    }

    fun getLocation(x0: Double, y0: Double, radius: Int): LatLng {
        val random = Random

        // Convert radius from meters to degrees
        val radiusInDegrees = (radius / 111000f).toDouble()
        val u: Double = random.nextDouble()
        val v: Double = random.nextDouble()
        val w = radiusInDegrees * Math.sqrt(u)
        val t = 2 * Math.PI * v
        val x = w * Math.cos(t)
        val y = w * Math.sin(t)

        // Adjust the x-coordinate for the shrinking of the east-west distances
        val new_x = x / Math.cos(Math.toRadians(y0))
        val foundLongitude = new_x + x0
        val foundLatitude = y + y0

        return LatLng(foundLongitude, foundLatitude)
    }
}
