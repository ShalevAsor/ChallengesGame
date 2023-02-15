package com.example.myapp.View

import android.Manifest
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.myapp.Controller.MapController
import com.example.myapp.DestinationsChallenge
import com.example.myapp.MainActivity
import com.example.myapp.Model.MarkerModel
import com.example.myapp.databinding.ActivityMapsBinding
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
import com.example.myapp.Model.Callback
import com.example.myapp.R

/**
 * This is the main activity , this class get the user current location in real time and display the location
 * represents by a marker , the marker location update in real time.
 * this class allows the user to start a challenge , add a challenge and navigate between the different activities
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

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
    private lateinit var mapFragment: SupportMapFragment

    //models
    private lateinit var markers: List<MarkerModel>
    private lateinit var challengeSelected: String
    private lateinit var currentMarker:MarkerModel

    //indicators
    private var clicked = false//allows only one click on the map when adding marker
    private var markersOnMap = false

    //const variables
    private val radius = 500.0
    private val defaultZoom = 12f

    //general
    private lateinit var mContext: Context
    private lateinit var userID:String

    //drawer navigation
    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView

    //allows to make a collection of markers with smoother view
    //private lateinit var clusterManager: ClusterManager<MyItem>

    private val CHANNEL_ID = "com.example.push_notification_channel"
    private val NOTIFICATION_ID = 123

    val sentNotifications = mutableMapOf<String, Boolean>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* init firebase variables */
        firebaseAuth = FirebaseAuth.getInstance()
//        if(firebaseAuth.currentUser == null){
//        Log.e("mydata2", "Value is: ${firebaseAuth.currentUser}")
//            intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }

        dbRef = FirebaseDatabase.getInstance().getReference("Markers")
        //init controller
//        if (firebaseAuth.currentUser == null) {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            finish()
//        } else {
//            userID = firebaseAuth.currentUser!!.uid
//            // Continue with the rest of your code
//        }
        if(firebaseAuth.currentUser == null){
            val intent = Intent(this, RegisterActivity::class.java)
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
        drawerLayout = findViewById(R.id.my_drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout,
            R.string.nav_open,
            R.string.nav_close
        )
        currentLocation = Location("")
        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        // to make the Navigation drawer icon always appear on the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // handle the navigation item click
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_profile -> {
                    intent = Intent(this, UserProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_top_scores -> {
                    setContentView(R.layout.fragment_top_scores)
                    val fragment:Fragment = TopScores()
                    val fragmentManager = supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.add(R.id.fragment_top_scores, fragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
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


        //load markers from database
        markers = mapController.getMarkers()
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        //save device last location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//        setMarkersOnMap()
//        getUserCurrentLocation()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
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
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLocation = it
                    // Sync the map with the current location
                    mapFragment.getMapAsync(this)
                }
            }

//        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        //mapFragment.getMapAsync(this)

        //since map


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
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
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
                        val currLatLng =  LatLng(currentLocation.latitude,currentLocation.longitude)
                        val markerLatLng = LatLng(marker.lat!!,marker.long!!)
                        val distance = mapController.calculateDistance(currLatLng, markerLatLng)
                        val isNotificationSent = sentNotifications.getOrDefault(marker.marker_id!!,false)
                        if (distance < 500 && !isNotificationSent) {
                                sentNotifications[marker.marker_id!!] = true
                            sendPushNotification(marker.chall_name!!)
                        }
                    }

                }
            }
        }
    }

    private fun sendPushNotification(challengeName: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_person)
            .setContentTitle("New Challenge Available")
            .setContentText("You are close to a challenge: $challengeName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
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

        getUserCurrentLocation()
        setMarkersOnMap()
        // Get the current location of the device and set the position of the map.

        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("My location!")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pinicon))

        //set map camera focus on the user location
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
        val markerTag = UUID.randomUUID().toString()
        myLocationMarker = mMap.addMarker(markerOptions)!!
        myLocationMarker.tag = markerTag
        //currMarker.title = "My location!"
        myLocationMarker.showInfoWindow()


        //set markers from database



        //get clicked location and add new marker
        addChallenge.setOnClickListener {
            Toast.makeText(this, "Click on desired location ", Toast.LENGTH_LONG).show()
            clicked = false
            mMap.setOnMapClickListener { latlng ->
                val location = LatLng(latlng.latitude, latlng.longitude)
                if (!clicked) {// can add only one marker in each "addChallenge" text clicked
                    clicked = true
                    challengeSelected = "Clicker" //DEFAULT
                    setAddChallengeDialog(markerTag, location)//in the future add top score here

                }
            }
        }
        focusLocation.setOnClickListener{
            val latLng = LatLng(currentLocation.latitude,currentLocation.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
        }
    }

    /**
     * This method add the Marker model on the real time  database
     */



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
        //load marker dialog
        if(p0.tag.toString()== myLocationMarker.tag){
            //user clicked on my location icon
            p0.title = "My Location!"
            p0.showInfoWindow()
        }
        val markerModel = getMarkerModel(p0.tag.toString())
        if (markerModel != null) {
            if(markerModel.marker_id != myLocationMarker.tag) {
                setDialog(markerModel)
            }
        }
        return true
    }

    /**
     * This method set the marker Dialog that allows the user to start the challenge
     * also the challenge information will display at this dialog
     */

    private fun setDialog(marker: MarkerModel?) {
        markerPopUp = Dialog(this)
        markerPopUp.setContentView(R.layout.marker_popup)
        val btnCloseDialog = markerPopUp.findViewById<TextView>(R.id.closePopup)
        val btnStartChallenge = markerPopUp.findViewById<Button>(R.id.marker_start)
        val description = markerPopUp.findViewById<TextView>(R.id.challenge_description)
        val topScore = markerPopUp.findViewById<TextView>(R.id.challenge_topScore)
        val challengeName = markerPopUp.findViewById<TextView>(R.id.challenge_name)
        val userScore = markerPopUp.findViewById<TextView>(R.id.challenge_userScore)

        if (marker != null) {
            description.text = marker.chall_description
            topScore.text = "Best score: " + marker.top_score.toString()
            challengeName.text = marker.chall_name
            mapController.getUserScoreForChallenge(marker.marker_id,userID , object : Callback {
                override fun onSuccess(score: Int) {
                    userScore.text = "Your Score: " + score
                }
            })
        }

        btnCloseDialog.setOnClickListener {
            markerPopUp.dismiss()
        }
        btnStartChallenge.setOnClickListener {
            startChallenge(marker)
        }
        markerPopUp.show()
    }



    /**
     * This method start new Challenge activity depend on each Challenge
     */

    private fun startChallenge(marker: MarkerModel?) {
        if (marker != null) {
            val intent: Intent


            when (marker.chall_name) {
                "Guess the flag" -> {
                    intent = Intent(this, FlagChallenge::class.java)
                    intent.putExtra("MARKER_ID",marker.marker_id)
                    startActivity(intent)
                }
                "Calculator" -> {
                    intent = Intent(this, MathChallenge::class.java)
                    intent.putExtra("MARKER_ID",marker.marker_id)
                    startActivity(intent)
                }
                "Clicker" -> {
                    intent = Intent(this, ButtonChallenge::class.java)
                    Log.i("markertee2", "the value is :$marker.marker_id")
                    intent.putExtra("MARKER_ID",marker.marker_id)
                    startActivity(intent)
                }
                "Guess The City" -> {
                    intent = Intent(this, GussTheCityChallenge::class.java)
                    intent.putExtra("MARKER_ID",marker.marker_id)
                    startActivity(intent)
                }
                "Logo Challenge" -> {
                    intent = Intent(this, LogoChallenge::class.java)
                    intent.putExtra("MARKER_ID",marker.marker_id)
                    startActivity(intent)
                }
                "Tap The Number" -> {
                    intent = Intent(this, TapTheNumChallenge::class.java)
                    intent.putExtra("MARKER_ID",marker.marker_id)
                    startActivity(intent)
                }
                "Destination" -> {
                    intent = Intent(this, DestinationsChallenge::class.java)
                    intent.putExtra("MARKER_ID",marker.marker_id)
                    startActivity(intent)
                }
                else -> {
                    Log.e("challenge", "Filed loading a challenge")
                    intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            //check if current marker score is lower then the score of the game




        }
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
            //setUpClusterer(marker)
            mMap.addMarker(markerOptions)?.tag = marker.marker_id
        }
    }

    /**
     * This method display the user dialog that allows to choose the specific challenge
     * to add to the map
     */
    private fun setAddChallengeDialog(markerTag: String, markerLocation: LatLng) {
        addMarkerPupUp = Dialog(mContext)
        addMarkerPupUp.setCancelable(false)
        addMarkerPupUp.setContentView(R.layout.add_marker_popup)
        val btnCloseDialog = addMarkerPupUp.findViewById<TextView>(R.id.closePopup)
        val btnSetChallenge = addMarkerPupUp.findViewById<Button>(R.id.set_challenge)

        val challenges = resources.getStringArray(R.array.challenges)
        val spinner = addMarkerPupUp.findViewById<Spinner>(R.id.challenge_spinner)
        if (spinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, challenges
            )
            spinner.adapter = adapter
            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    challengeSelected = challenges[position]
                    //wait for the user to click "select"
                    btnSetChallenge.setOnClickListener {
                        val currMarkerTag = UUID.randomUUID().toString()
                        val markerIconID = getMarkerIcon(challengeSelected)
                        val challengeDescription = getDescription(challengeSelected)
                        val currMarker = mMap.addMarker(MarkerOptions().position(markerLocation).icon(
                            BitmapDescriptorFactory.fromResource(markerIconID)
                        ))!!
                        currMarker.tag = currMarkerTag
                        Toast.makeText(mContext, "Success ", Toast.LENGTH_SHORT).show()
                        //add to database
                        mapController.addMarker(
                            currMarkerTag,
                            challengeSelected,
                            challengeDescription,
                            markerLocation.latitude,
                            markerLocation.longitude,
                            0,
                            0
                        )
                        updateMarkersOnTheList()
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(markerLocation))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, 15.0f))

                        addMarkerPupUp.dismiss()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    Toast.makeText(mContext, "Please choose challenge ", Toast.LENGTH_SHORT).show()
                }
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
    //    private fun setUpClusterer(marker:MarkerModel) {
//        // Initialize the manager with the context and the map.
//        // (Activity extends context, so we can pass 'this' in the constructor.)
//        clusterManager = ClusterManager(mContext, mMap)
//
//        // Point the map's listeners at the listeners implemented by the cluster
//        // manager.
//        mMap.setOnCameraIdleListener(clusterManager)
//        //mMap.setOnMarkerClickListener(clusterManager)
//
//        // Add cluster items (markers) to the cluster manager.
//        addItems(marker)
//    }
//
//    private fun addItems(markerModel: MarkerModel) {
//
//
//        // Create a cluster item for the marker and set the title and snippet using the constructor.
//        val infoWindowItem = MyItem(markerModel.lat as Double, markerModel.long as Double, markerModel.chall_name as String
//            ,markerModel.chall_description as  String)
//
//        // Add the cluster item (marker) to the cluster manager.
//        clusterManager.addItem(infoWindowItem)
//        }
//    }
}




