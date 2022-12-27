package com.example.myapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.util.Log.DEBUG
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapp.BuildConfig.DEBUG
import com.example.myapp.MathChallenge
import com.example.myapp.Model.MarkerModel
import com.example.myapp.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var currentLocation :Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101
    private lateinit var markers: List<MarkerModel>
    private lateinit var currMarker:Marker
    private lateinit var markerPopUp: Dialog
    private lateinit var addMarkerPupUp:Dialog
    private lateinit var mContext:Context
    private lateinit var challengeSelected:String
    private val radius = 500.0
    private val defaultZoom = 12f
    // for map custom design

    lateinit var menu_bar: LinearLayout
    lateinit var addChallenge: TextView
    // indicator - allows only one click on the map when adding marker
    private var clicked = false
    private var locationPermissionGranted = false
    private lateinit var fetchData: FetchData

    private val KEY_CAMERA_POSITION = "camera_position"
    private val KEY_LOCATION = "location"

    //companion onject
    private lateinit var cameraPosition: CameraPosition
    // Realtime database variable
    private lateinit var dbRef: DatabaseReference
   // private lateinit var markers:List<MarkerModel>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        /* set layer  */
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        menu_bar = findViewById(R.id.map_bar)
        addChallenge = findViewById(R.id.addChallengeBtn)

        //save device last location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // go to main page
        binding.mapMainBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        dbRef = FirebaseDatabase.getInstance().getReference("Markers")
        //load markers from database
        fetchData = FetchData()
        markers = fetchData.Markers_location()

        getUserCurrentLocation()
        /* manage view */
        // go to main page
        binding.mapMainBtn.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        //add top scores btn here

    }
    // if there is no location permission then ask from the user permission and get current location
    private fun getUserCurrentLocation(){
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
            !=PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),permissionCode)
            return
        }

        val getLocation = fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                location ->
            if(location != null){
                currentLocation  = location
                Toast.makeText(this,currentLocation .latitude.toString() + " " +
                        currentLocation .longitude.toString(), Toast.LENGTH_LONG).show()

                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map_frag) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }
    /* when the user gave permission get the user location */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            permissionCode -> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getUserCurrentLocation()
            }
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
        // Get the current location of the device and set the position of the map.
        val latLng = LatLng(currentLocation.latitude,currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("My location!")

        //set map camera focus on the user location

        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f))
        val markerTag = UUID.randomUUID().toString()
        currMarker = mMap.addMarker(markerOptions)!!
        currMarker.tag = markerTag

        //set markers from database
        setMarkersOnMap()

        //get clicked location and add new marker
        addChallenge.setOnClickListener{


            Toast.makeText(this,"Click on desired location ",Toast.LENGTH_LONG).show()
            clicked = false
            mMap.setOnMapClickListener { latlng ->
                val location = LatLng(latlng.latitude, latlng.longitude)
                if(!clicked) {// can add only one marker in each "addChallenge" text clicked
                    clicked = true
                    challengeSelected = "Clicker" //DEFAULT
                    setAddChallengeDialog(markerTag,location)//in the future add top score here

                }
            }
        }
    }

    /**
     * This method add the Marker model in real time to database
     */

    private fun addMarkerToDatabase(
        markerTag: String,
        challengeSelected: String,
        challengeDescription: String,
        latitude: Double,
        longitude: Double,
        topScore: Int,
        personalScore: Int
    ) {
        val markerId = dbRef.push().key!!
        val game = MarkerModel(markerTag,challengeSelected ,challengeDescription,latitude,longitude,topScore,personalScore)
        dbRef.child(markerId).setValue(game)
            .addOnCompleteListener {
                Log.e( "www","Marker was add to DB successfully")
            }.addOnFailureListener { err ->
                Log.e( "mmm","Marker was add to DB successfully")
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
        //load marker dialog
        Log.e("ttt",p0.tag.toString())
        val markerModel = getMarkerModel(p0.tag.toString())
        setDialog(markerModel)
        return true
    }

    /**
     * This method set the marker Dialog thats allows the user to start the challenge
     * also the challenge information will display at this dialog
     */

    private fun setDialog(marker:MarkerModel?) {
        // set view and view objects
        markerPopUp = Dialog(this)
        markerPopUp.setCancelable(false)
        markerPopUp.setContentView(R.layout.marker_popup)
        val btnCloseDialog = markerPopUp.findViewById<TextView>(R.id.closePopup)
        val btnStartChallenge = markerPopUp.findViewById<Button>(R.id.marker_start)
        val description = markerPopUp.findViewById<TextView>(R.id.challenge_description)
        val topScore = markerPopUp.findViewById<TextView>(R.id.challenge_topScore)
        val challengeName = markerPopUp.findViewById<TextView>(R.id.challenge_name)

        //set clicked marker data
        if (marker != null) {
            description.text = marker.chall_description
            topScore.text = marker.top_score.toString()
            challengeName.text = marker.chall_name
        }
        //close dialog here
        btnCloseDialog.setOnClickListener{
            markerPopUp.dismiss()
        }
        btnStartChallenge.setOnClickListener{
            //add code to start game here
            startChallenge(marker)
        }
        markerPopUp.show()
    }

    /**
     * This method start new Challenge activity depend on each Challenge
     */

    private fun startChallenge(marker: MarkerModel?) {
        if (marker != null) {
            val intent:Intent
            when(marker.chall_name){
                "Guess the flag" -> {
                   intent = Intent(this, FlagChallenge::class.java)
                    startActivity(intent)
                }
                "Calculator" -> {
                    intent = Intent(this, MathChallenge::class.java)
                    startActivity(intent)
                }
                "Clicker" -> {
                    intent = Intent(this, ButtonChallenge::class.java)
                    startActivity(intent)
                }
                else -> {
                    Log.e("challenge","Filed loading a challenge")
                    intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }


            }
        }
    }

    /**
     * This method return the markerModel by the given marker tag
     */

    private fun getMarkerModel(id: String): MarkerModel? {
        for(marker in markers){
            if(id == marker.marker_id){
                return marker
            }
        }
        return null
    }

    private fun startRandomChallenge(){

    }

    /**
     * This method add the markers from the markers list  to the map
     */
    private fun setMarkersOnMap(){
        for(marker in markers){
            //add check in case that the marker ttl is over
            val latLng = LatLng(marker.lat as Double,marker.long as Double)
            val markerOptions = MarkerOptions().position(latLng).title("My location!")
            mMap.addMarker(markerOptions)?.tag = marker.marker_id

        }
    }

    /**
     * This method display the user dialog that allows to choose the specific challenge
     * to add to the map
     */
    private fun setAddChallengeDialog(markerTag:String,markerLocation: LatLng) {
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
                        val challengeDescription = fetchData.getDescription(challengeSelected)
                        currMarker = mMap.addMarker(MarkerOptions().position(markerLocation))!!
                        currMarker.tag = markerTag
                        Toast.makeText(mContext, "Success ", Toast.LENGTH_SHORT).show()
                        //add to database
                        addMarkerToDatabase(markerTag,challengeSelected ,challengeDescription,markerLocation.latitude,markerLocation.longitude,0,0)
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


    }
