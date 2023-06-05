package com.geochamp.myapp.View

import android.Manifest
import android.app.ActivityManager
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.geochamp.myapp.Controller.MapController
import com.geochamp.myapp.MainActivity
import com.geochamp.myapp.Model.MarkerModel
import com.geochamp.myapp.databinding.ActivityMapsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.maps.android.clustering.ClusterManager
import java.util.*
import com.geochamp.myapp.Model.Callback
import com.geochamp.myapp.Model.MarkerItem
import com.geochamp.myapp.R
import com.skydoves.powerspinner.PowerSpinnerView
import de.hdodenhof.circleimageview.CircleImageView

/**
 * This is the main activity , this class get the user current location in real time and display the location
 * represents by a marker , the marker location update in real time.
 * this class allows the user to start a challenge , add a challenge and navigate between the different activities
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener,ClusterManager.OnClusterItemClickListener<MarkerItem> {

    //firebase variables
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    //map controller
    private lateinit var mapController: MapController

    //map variables
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101

    //view variables
    private lateinit var myLocationMarker: Marker
    private lateinit var markerPopUp: Dialog
    private lateinit var addMarkerPupUp: Dialog
    private lateinit var addChallenge: View // floating button add challenge
    private lateinit var focusLocation: View // focus the camera floating button
    private lateinit var userFullName: TextView
    private lateinit var userScore: TextView
    private lateinit var userImage: CircleImageView

    //models
    private lateinit var markers: List<MarkerModel>
    private lateinit var challengeSelected: String

    //indicators
    private var clicked = false//allows only one click on the map when adding marker
    private var nightMode = false

    //const variables
    private val radius = 500.0
    private val defaultZoom = 12f

    // Set default value to 18 to represent 6 pm
    val defaultDarkMapHour = 18

    //general
    private lateinit var mContext: Context
    private lateinit var userID: String

    //drawer navigation
    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView


    private val CHANNEL_ID = "com.example.push_notification_channel"
    private val NOTIFICATION_ID = 123

    //clusterManager to manage the markers
    //allows to make a collection of markers with smoother view
    private lateinit var clusterManager: ClusterManager<MarkerItem>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* init firebase variables */
        firebaseAuth = FirebaseAuth.getInstance()

        dbRef = FirebaseDatabase.getInstance().getReference("Markers")


        if (firebaseAuth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        userID = firebaseAuth.currentUser!!.uid



        mapController = MapController(this)

        /* init view */
        mContext = this
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //floating action button
        addChallenge = findViewById(R.id.addChallengeBtn)
        focusLocation = findViewById(R.id.focus_camera_location_btn)
        createNotificationChannel()

        /* drawer layout instance to toggle the menu icon to open
         drawer and back button to close drawer */
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawerLayout,
            R.string.nav_open,
            R.string.nav_close
        )
        //add banner with user data
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bannerLayout = inflater.inflate(R.layout.map_banner_layout, null)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.TOP
        bannerLayout.layoutParams = params
        userFullName = bannerLayout.findViewById(R.id.user_name)
        userScore = bannerLayout.findViewById(R.id.user_score)
        userImage = bannerLayout.findViewById(R.id.map_user_profile_image)
        mapController.getUser(userID) { user ->
            if (user != null) {
                userFullName.text = user.firstName + " " + user.lastName
                userScore.text = "Score: " + user.personalScore.toString()
                if (user.imageUrl.isNullOrEmpty()) {
                    Glide.with(applicationContext)
                        .load(R.drawable.ic_defalut_profile_image)
                        .into(userImage)
                } else {
                    Glide.with(applicationContext)
                        .load(user.imageUrl)
                        .into(userImage)
                }
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val mapView = mapFragment.view as ViewGroup
        mapView.addView(bannerLayout)
        currentLocation = Location("")
        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        // to make the Navigation drawer icon always appear on the action bar
        //supportActionBar?.title = "Geo-Champ"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //load markers from database
        markers = mapController.getMarkers()
        // mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment


        dbRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                addMarkersToCluster(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // handle marker changes
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // handle marker removals
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // handle marker movements
            }

            override fun onCancelled(error: DatabaseError) {
                // handle error
            }
        })



        //save device last location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                permissionCode
            )
            return
        }
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLocation = it
                    // Sync the map with the current location
                    mapFragment.getMapAsync(this)
                }
            }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    //clicked on android phone back btn
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            super.onBackPressed()
            finish()
        }
    }



    /**
     * This method ask location permissions from the user iff there arent permissions
     * then init the currentLocation and sync the map
     */
    // if there is no location permission then ask from the user permission and get current location
    private fun getUserCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                permissionCode
            )
            return
        }

        val locationRequest = LocationRequest().apply {
            interval = 1000
            fastestInterval = 500
            priority = if (isAppInForeground()) {
                LocationRequest.PRIORITY_HIGH_ACCURACY
            } else {
                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            if (p0 != null && p0.locations.isNotEmpty()) {
                currentLocation = p0.locations[0]
                if (::myLocationMarker.isInitialized && currentLocation != null) {
                    myLocationMarker.position = LatLng(currentLocation.latitude, currentLocation.longitude)
                    //check if the user location is close to marker :
                    for (marker in markers) {
                        val preferences = getSharedPreferences("com.example.com.geochamp.com.geochamp.myapp.notifications", Context.MODE_PRIVATE)
                        val isNotificationSent = preferences.getBoolean(marker.marker_id!!, false)
                        val currLatLng =  LatLng(currentLocation.latitude,currentLocation.longitude)
                        val markerLatLng = LatLng(marker.lat!!,marker.long!!)
                        val distance = mapController.calculateDistance(currLatLng, markerLatLng)
                        if (distance < 500 && !isNotificationSent) {
                            preferences.edit().putBoolean(marker.marker_id!!, true).apply()
                            sendPushNotification(marker.chall_name!!)
                        }
                    }

                }
            }
        }
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        return appProcesses.any { it.processName == packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
    }

    /**
     * The app is not in the background anymore , get the most accurate location as possible
     */
    override fun onResume() {
        super.onResume()
        getUserCurrentLocation()
    }

    /**
     * The app is in the background , change to PRIORITY_BALANCED_POWER_ACCURACY to save battery
     * also take less updates of the user location
     */
    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun sendPushNotification(challengeName: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_bell)
            .setContentTitle("New Challenge Available")
            .setContentText("You are close to a challenge: $challengeName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "New Challenge Available"
            val descriptionText = "The user is under 500 meters radius from a challenge"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }




    /* when the user gave permission get the user location */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getUserCurrentLocation()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     */
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.setOnMarkerClickListener(this)

        //if the time is after 6 pm change to night style map
        val currentTime = Calendar.getInstance().time
        val currentHour = currentTime.hours
        if(currentHour >= defaultDarkMapHour) {
            nightMode = true
            // Load the map style JSON file
            val mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                this, R.raw.map_style
            )
            mMap.setMapStyle(mapStyleOptions)
            //change the floating action button color and the banner color
        }
        else{
            nightMode = false
        }

        getUserCurrentLocation()
       // setMarkersOnMap()


        // Get the current location of the device and set the position of the map.

        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location))
        //set map camera focus on the user location
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
        val markerTag = UUID.randomUUID().toString()
        myLocationMarker = mMap.addMarker(markerOptions)!!
        myLocationMarker.tag = markerTag
        //myLocationMarker.showInfoWindow()

//        //cluster
         clusterManager = ClusterManager(mContext, mMap)
        val clusterRenderer = MarkerRenderer(this, mMap, clusterManager)
        clusterManager.renderer = clusterRenderer
//
//      // Point the map's listeners at the listeners implemented by the cluster

        mMap.setOnCameraIdleListener(clusterManager)
        clusterManager.setOnClusterItemClickListener (this)
        for (marker in markers){
            addItems(marker)
        }




        //get clicked location and add new marker
        addChallenge.setOnClickListener {
            Toast.makeText(this, "Click on desired location ", Toast.LENGTH_LONG).show()
            clicked = false
            addChallenge.visibility = View.INVISIBLE
            focusLocation.visibility = View.INVISIBLE
            mMap.setOnMapClickListener { latlng ->
                val location = LatLng(latlng.latitude, latlng.longitude)
                if (!clicked) {// can add only one marker in each "addChallenge" text clicked
                    clicked = true
                    challengeSelected = "Clicker" //DEFAULT
                    setAddChallengeDialog(markerTag, location)//in the future add top score here
                    addChallenge.visibility = View.VISIBLE
                    focusLocation.visibility = View.VISIBLE

                }
            }
        }

        focusLocation.setOnClickListener{
            val latLng = LatLng(currentLocation.latitude,currentLocation.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
        }

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.nav_profile -> {
                    intent = Intent(this, UserProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_top_scores -> {
                    intent = Intent(this, TopScores::class.java)
                    startActivity(intent)

                }
                R.id.nav_logout -> {
                    firebaseAuth.signOut()
                    signOutGoogle()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                }
            }
            true
        }
    }

    /**
     * This method allows to draw a circle where the center of the circle is Marker
     * this circle represents the max radius that the payer can start a challenge
     */
    private fun drawCircle(location: LatLng) {
        val circleOptions = CircleOptions()
        //specify the center of the circle
        circleOptions.center(location)
        circleOptions.radius(radius)
        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);

        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);

        // Border width of the circle
        circleOptions.strokeWidth(2F);

        mMap.addCircle(circleOptions)
    }
    /**
     * When the marker has clicked open the popup dialog
     * Need to load the popup dialog according to the challenge type
     */

    override fun onMarkerClick(p0: Marker): Boolean {
        //if the user click on current location marker display info window with my location title
        if(p0.tag.toString()== myLocationMarker.tag){
            //user clicked on my location icon
            p0.title = "My Location!"
            p0.showInfoWindow()
        }
        return true
    }

    /**
     * This method set the marker Dialog that allows the user to start the challenge
     * also the challenge information will display at this dialog
     */

    private fun setDialog(marker: MarkerModel?) {
        markerPopUp = Dialog(this)
        markerPopUp.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        markerPopUp.setContentView(R.layout.marker_popup)
        val btnCloseDialog = markerPopUp.findViewById<TextView>(R.id.closePopup)
        val btnStartChallenge = markerPopUp.findViewById<Button>(R.id.marker_start)
        val description = markerPopUp.findViewById<TextView>(R.id.challenge_description)
        val topScore = markerPopUp.findViewById<TextView>(R.id.challenge_topScore)
        val challengeName = markerPopUp.findViewById<TextView>(R.id.challenge_name)
        val userScore = markerPopUp.findViewById<TextView>(R.id.challenge_userScore)
        var userTopScore = 0
        var challengeTopScore = 0

        if (marker != null) {
            description.text = marker.chall_description
            mapController.getChallengeTopScores(marker.marker_id,object : Callback {
                override fun onSuccess(score: Int) {
                    topScore.text = "Best score: " + score
                    challengeTopScore = score
                }

            })
            challengeName.text = marker.chall_name
            mapController.getUserScoreForChallenge(marker.marker_id,userID , object : Callback {
                override fun onSuccess(score: Int) {
                    userScore.text = "Your Score: " + score
                    userTopScore = score
                }
            })
        }

        btnCloseDialog.setOnClickListener {
            markerPopUp.dismiss()
        }
        btnStartChallenge.setOnClickListener {
            if (marker != null) {
                startChallenge(marker.chall_name,marker.marker_id,challengeTopScore,userTopScore)
            }
            markerPopUp.dismiss()
        }
        markerPopUp.show()
    }



    /**
     * This method start new Challenge activity depend on each Challenge
     */

    private fun startChallenge(challengeName:String?,markerId:String?,challengeTopScore : Int? , userTopScore: Int?) {
        if (challengeName != null && markerId != null) {
            val intent: Intent
            val requestCode = 111 // request code for the results of the challenge


            when (challengeName) {
                "Guess the flag" -> {
                    intent = Intent(this, FlagChallenge::class.java)
                    intent.putExtra("MARKER_ID",markerId)
                    intent.putExtra("TOP_SCORES",challengeTopScore)
                    intent.putExtra("USER_TOP_SCORE",userTopScore)
                    intent.putExtra("CHALL_NAME",challengeName)
                    //startActivity(intent)
                    startActivityForResult(intent,requestCode)
                }
                "Calculator" -> {
                    intent = Intent(this, MathChallenge::class.java)
                    intent.putExtra("MARKER_ID",markerId)
                    intent.putExtra("TOP_SCORES",challengeTopScore)
                    intent.putExtra("USER_TOP_SCORE",userTopScore)
                    intent.putExtra("CHALL_NAME",challengeName)
                    startActivityForResult(intent,requestCode)
                }
                "Clicker" -> {
                    intent = Intent(this, ButtonChallenge::class.java)
                    Log.i("markertee2", "the value is :$markerId")
                    intent.putExtra("MARKER_ID",markerId)
                    intent.putExtra("TOP_SCORES",challengeTopScore)
                    intent.putExtra("USER_TOP_SCORE",userTopScore)
                    intent.putExtra("CHALL_NAME",challengeName)
                    startActivityForResult(intent,requestCode)
                }
                "Guess The City" -> {
                    intent = Intent(this, GussTheCityChallenge::class.java)
                    intent.putExtra("MARKER_ID",markerId)
                    intent.putExtra("TOP_SCORES",challengeTopScore)
                    intent.putExtra("USER_TOP_SCORE",userTopScore)
                    intent.putExtra("CHALL_NAME",challengeName)
                    startActivityForResult(intent,requestCode)
                }
                "Logo Challenge" -> {
                    intent = Intent(this, LogoChallenge::class.java)
                    intent.putExtra("MARKER_ID",markerId)
                    intent.putExtra("TOP_SCORES",challengeTopScore)
                    intent.putExtra("USER_TOP_SCORE",userTopScore)
                    intent.putExtra("CHALL_NAME",challengeName)
                    startActivityForResult(intent,requestCode)
                }
                "Tap The Number" -> {
                    intent = Intent(this, TapTheNumChallenge::class.java)
                    intent.putExtra("MARKER_ID",markerId)
                    intent.putExtra("TOP_SCORES",challengeTopScore)
                    intent.putExtra("USER_TOP_SCORE",userTopScore)
                    intent.putExtra("CHALL_NAME",challengeName)
                    startActivityForResult(intent,requestCode)
                }
                "Destination" -> {
                    intent = Intent(this, DestinationsChallenge::class.java)
                    intent.putExtra("MARKER_ID",markerId)
                    intent.putExtra("TOP_SCORES",challengeTopScore)
                    intent.putExtra("USER_TOP_SCORE",userTopScore)
                    intent.putExtra("CHALL_NAME",challengeName)
                    startActivityForResult(intent,requestCode)
                }
                else -> {
                    Log.e("challenge", "Filed loading a challenge")
                    intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("onActivityResult", "requestCode: $requestCode, resultCode: $resultCode")

        if (requestCode == 111 && resultCode == RESULT_OK) { // modify the request code to match the one used above
            Log.i("onActivityResultsCodeIs","The reulsts code is $resultCode")
            val score = data?.getIntExtra("SCORE", 0) ?: 0 // retrieve the score from the intent data
            val markerId = data?.getStringExtra("MARKER_ID")
            val challengeName = data?.getStringExtra("CHALL_NAME")
            val oldChallengeTopScore = data?.getIntExtra("OLD_CH_TOP_SCORE",0) ?: 0
            val oldUserTopScore = data?.getIntExtra("OLD_USER_TOP_SCORE",0) ?: 0
            showPerformanceDialog(score,markerId,challengeName,oldUserTopScore,oldChallengeTopScore) // display the performance dialog with the user's score
        }
    }

    private fun showPerformanceDialog(currScore: Int,markerId : String?,challengeName:String?,oldUserTopScore : Int , oldChallengeTopScore:Int) {
        val performanceDialog = Dialog(this)
        performanceDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        performanceDialog.setContentView(R.layout.performance_popup)
        val btnCloseDialog = performanceDialog.findViewById<TextView>(R.id.p_close)
        val topScores = performanceDialog.findViewById<TextView>(R.id.p_top_scores)
        val userTopScores = performanceDialog.findViewById<TextView>(R.id.p_user_top_score)
        val userCurrentScore = performanceDialog.findViewById<TextView>(R.id.p_user_score)
        val btnTryAgain = performanceDialog.findViewById<Button>(R.id.p_again)
        val btnOk = performanceDialog.findViewById<Button>(R.id.p_ok)
        val description = performanceDialog.findViewById<TextView>(R.id.p_description)
        var breakTopScores = false
        userCurrentScore.text = "Your Score: $currScore"
        var newChallTopScore = oldChallengeTopScore
        var newUserTopScore = oldUserTopScore

        mapController.getUserScoreForChallenge(markerId,userID , object : Callback {
            override fun onSuccess(score: Int) {
                Log.i("userTopScore", "the user top score in this challenge is : $score")
                userTopScores.text = "Your Top score: $score"
            }
        })

        mapController.getChallengeTopScores(markerId,object : Callback {
            override fun onSuccess(score: Int) {
                Log.i("challengetopscore", "the challenge top score in this challenge is : $score the marker id is : $markerId")
                topScores.text = "Top score: $score"
            }

        })


        if(currScore > oldChallengeTopScore){
            description.text = getString(R.string.p_beat_top_scores)
            newUserTopScore = currScore
            newChallTopScore = currScore
            Log.i("whatText", "we are here1 : $currScore , the old ch top scores is : $oldChallengeTopScore")

        }
        else if(currScore > oldUserTopScore ){
            description.text = getString(R.string.p_beat_user_scores)
            newUserTopScore = currScore
            Log.i("whatText", "we are here2 : $currScore")
        }
        else{
            Log.i("whatText", "we are here3 : $currScore")
            description.text = getString(R.string.p_bad_scores)
        }

        btnCloseDialog.setOnClickListener {
            performanceDialog.dismiss()
        }
        btnOk.setOnClickListener {
            performanceDialog.dismiss()
        }
        btnTryAgain.setOnClickListener {
            Log.i("TRY AGAIN","visited")
            performanceDialog.dismiss()
            startChallenge(challengeName,markerId,newChallTopScore,newUserTopScore)

        }

        performanceDialog.show()
    }

    /**
     * This method return the markerModel by the given marker tag
     */

    private fun getMarkerModel(id: String): MarkerModel? {
        for (marker in markers) {
            if (id == marker.marker_id) {
                return marker
            }
        }
        return null
    }

    /**
     * This method add the markers from the markers list  to the map
     */
    private fun setMarkersOnMap() {
        for (marker in markers) {
            //add check in case that the marker ttl is over
            val latLng = LatLng(marker.lat as Double, marker.long as Double)
            val iconID = getMarkerIcon(marker.chall_name as String)
            val markerOptions = MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(iconID))
            mMap.addMarker(markerOptions)?.tag = marker.marker_id
        }
    }


    /**
     * This method display the user dialog that allows to choose the specific challenge
     * to add to the map
     */
    private fun setAddChallengeDialog(markerTag: String, markerLocation: LatLng) {
        addMarkerPupUp = Dialog(mContext)
        addMarkerPupUp.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addMarkerPupUp.setCancelable(false)
        addMarkerPupUp.setContentView(R.layout.add_marker_popup)
        val btnCloseDialog = addMarkerPupUp.findViewById<TextView>(R.id.closePopup)
        val btnSetChallenge = addMarkerPupUp.findViewById<Button>(R.id.set_challenge)

        val challenges = resources.getStringArray(R.array.challenges)
        val spinner = addMarkerPupUp.findViewById<PowerSpinnerView>(R.id.challenge_spinner)
        if (spinner != null) {
            spinner.setItems(challenges.toList())
            spinner.setOnSpinnerItemSelectedListener<String> { _, _, index, _ ->
                challengeSelected = challenges[index]
            }
            btnSetChallenge.setOnClickListener {
                //checks if the user has enought points to open a challenge
                var flag=true
               // mapController.enoughPoints(userID)
                mapController.enoughPoints(userID) { hasEnoughPoints ->
                    if (hasEnoughPoints) {
                        // User has enough points, proceed with the logic here
                        val currMarkerTag = UUID.randomUUID().toString()
                        val markerIconID = getMarkerIcon(challengeSelected)
                        val challengeDescription = getDescription(challengeSelected)
                        // val currMarker = mMap.addMarker(MarkerOptions().position(markerLocation).icon(
                        // BitmapDescriptorFactory.fromResource(markerIconID)
                        // ))!!
                        // currMarker.tag = currMarkerTag
                        val markerToAdd = MarkerModel(
                            currMarkerTag,
                            userID,
                            challengeSelected,
                            challengeDescription,
                            markerLocation.latitude,
                            markerLocation.longitude,
                            0,
                            Date().time
                        )
                        // addItems(markerToAdd)
                        Toast.makeText(mContext, "Success ", Toast.LENGTH_SHORT).show()
                        // add to database
                        mapController.addMarker(
                            currMarkerTag,
                            challengeSelected,
                            challengeDescription,
                            markerLocation.latitude,
                            markerLocation.longitude,
                            0,
                            Date().time
                        )
                        mapController.payForChallenge(userID)
                    } else {
                        // User does not have enough points, show an error message or take appropriate action
                        Toast.makeText(mContext, "To open a challenge you need 50 points!", Toast.LENGTH_SHORT).show()
                    }
                }
//                Log.e("flag", "the message is $flag")
//                if(flag){
//                    val currMarkerTag = UUID.randomUUID().toString()
//                    val markerIconID = getMarkerIcon(challengeSelected)
//                    val challengeDescription = getDescription(challengeSelected)
//                    // val currMarker = mMap.addMarker(MarkerOptions().position(markerLocation).icon(
//                    // BitmapDescriptorFactory.fromResource(markerIconID)
//                    // ))!!
//                    // currMarker.tag = currMarkerTag
//                    val markerToAdd = MarkerModel(
//                        currMarkerTag,
//                        challengeSelected,
//                        challengeDescription,
//                        markerLocation.latitude,
//                        markerLocation.longitude,
//                        0,
//                        Date().time
//                    )
//                    // addItems(markerToAdd)
//                    Toast.makeText(mContext, "Success ", Toast.LENGTH_SHORT).show()
//                    // add to database
//                    mapController.addMarker(
//                        currMarkerTag,
//                        challengeSelected,
//                        challengeDescription,
//                        markerLocation.latitude,
//                        markerLocation.longitude,
//                        0,
//                        Date().time
//                    )
//                    mapController.payForChallenge(userID)
//                }
//                else{
//                    Toast.makeText(mContext, "To open a challenge you need 50 points!", Toast.LENGTH_SHORT).show()
//                }


                updateMarkersOnTheList()
                mMap.animateCamera(CameraUpdateFactory.newLatLng(markerLocation))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, 15.0f))
                addMarkerPupUp.dismiss()
                // refreshView()

        }
            btnCloseDialog.setOnClickListener {
                addMarkerPupUp.dismiss()
            }
            addMarkerPupUp.show()
        }
    }

    private fun getDescription(name: String): String {
        val challenges = resources.getStringArray(R.array.challenges_description)
        for (challenge in challenges) {
            val challengePair = challenge.split(":")
            if (challengePair[0] == name) {
                return challengePair[1]
            }
        }
        return "Exception"
    }


    private fun signOutGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

    }

    /**
     * This method return the marker icon id depend the name of the given challenge
     */

    private fun getMarkerIcon(name:String):Int {
        when(name){
            "Guess the flag" -> {
                return R.drawable.flagicon
            }
            "Calculator" -> {
                return R.drawable.calcuicon
            }
            "Clicker" -> {
                return R.drawable.clickericon
            }
            "Guess The City" -> {
                return R.drawable.cityicon
            }
            "Logo Challenge" -> {
                return R.drawable.logoicon
            }
            "Tap The Number" -> {
                return R.drawable.numbericon
            }
            "Destination" -> {
                return R.drawable.destination_ic
            }
            else -> {
                Log.e("icons", "Filed loading icon")
                return R.drawable.pinicon
            }

        }

    }
    private fun updateMarkersOnTheList(){
        markers = mapController.getMarkers()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun addItems(markerModel: MarkerModel) {
        // Create a cluster item for the marker
        val infoWindowItem = MarkerItem(markerModel.lat as Double, markerModel.long as Double,markerModel.marker_id as String, markerModel.chall_name as String
            ,markerModel.chall_description as  String)

        // Add the cluster item (marker) to the cluster manager.
        clusterManager.addItem(infoWindowItem)

    }

    override fun onClusterItemClick(item: MarkerItem?): Boolean {

        val markerModel = item?.let { getMarkerModel(it.id) }
        Log.i("onlyRandomKey","isvisited")
        if (markerModel != null) {
            if(markerModel.marker_id != myLocationMarker.tag) {
                //check the distance - allows the user start challenge iff the user distance is less than 500 meters
                val markerLatLng = LatLng(markerModel.lat!!,markerModel.long!!)
                val currLatLng =  LatLng(currentLocation.latitude,currentLocation.longitude)
                val distance = mapController.calculateDistance(currLatLng, markerLatLng)
                if(distance<500) {
                    setDialog(markerModel)
                }
                else{
                    Toast.makeText(mContext, "You cant start a challenge here , you need to get closer! ", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return true
    }

    private fun refreshView(){
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun addMarkersToCluster(snapshot: DataSnapshot) {
        if (!::clusterManager.isInitialized) {
            // clusterManager has not been initialized yet, so we can't add any markers
            return
        }
        val marker = snapshot.getValue(MarkerModel::class.java)
        if (marker != null) {
            Log.i("dbListener", "the value is :$marker")

            // Create a cluster item for the marker
            val infoWindowItem = MarkerItem(marker.lat as Double, marker.long as Double,marker.marker_id as String, marker.chall_name as String
                ,marker.chall_description as  String)

            // Add the cluster item (marker) to the cluster manager.
            clusterManager.addItem(infoWindowItem)
            clusterManager.cluster()
        }
    }


}




