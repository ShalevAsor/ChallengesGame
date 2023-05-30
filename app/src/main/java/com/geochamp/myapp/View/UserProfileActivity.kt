package com.geochamp.myapp.View

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.geochamp.myapp.Controller.UserProfileController
import com.geochamp.myapp.R
import com.geochamp.myapp.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
/**
 * UserProfileActivity is an `AppCompatActivity` that allows users to view and edit their personal profile information.
 * The activity displays information such as the user's full name, first name, last name, email, and profile image.
 *
 * @property binding the data binding object for the `activity_user_profile` layout
 * @property userProfileController the controller responsible for managing user profile data
 * @property firebaseAuth the instance of FirebaseAuth used to authenticate the user
 * @property dbRef the database reference to the UserModel data in Firebase
 * @property uid the user's unique identifier obtained from FirebaseAuth
 * @property pickImage a constant used to identify the image picker activity when it returns result
 * @property imageUri the URI of the image selected by the user
 *
 */
class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding:ActivityUserProfileBinding
    private lateinit var userProfileController:UserProfileController
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var uid:String
    private val pickImage = 100
    private var imageUri: Uri? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_user_profile)
        firebaseAuth = FirebaseAuth.getInstance()
        userProfileController = UserProfileController(this)
        uid = firebaseAuth.currentUser?.uid.toString()
        val userFullName:TextView = findViewById(R.id.user_profile_full_name)
        val userFirstName:TextView = findViewById(R.id.user_profile_first_name)
        val userLastName:TextView = findViewById(R.id.user_profile_last_name)
        val userEmail:TextView = findViewById(R.id.user_profile_email)
        val userProfileImage: CircleImageView = findViewById(R.id.user_profile_image)
        val editEmail:EditText = findViewById(R.id.user_profile_email_edit)
        val editFirstName:EditText = findViewById(R.id.user_profile_first_name_edit)
        val editLastName:EditText = findViewById(R.id.user_profile_last_name_edit)
        val editBtn:ImageView = findViewById(R.id.ic_edit_profile_1)
        val saveBtn:Button = findViewById(R.id.user_profile_save_btn)
        val statsBtn:Button = findViewById(R.id.user_profile_stats_btn)
        val uploadImageBtn = findViewById<ImageView>(R.id.ic_edit_profile_upload_picture)
        editEmail.visibility = View.GONE
        editFirstName.visibility = View.GONE
        editLastName.visibility = View.GONE
        saveBtn.visibility = View.GONE
        uploadImageBtn.visibility = View.GONE
//        supportActionBar?.title = "Geo-Champ"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbRef = FirebaseDatabase.getInstance().getReference("UserModel")
        Log.i("uid", "the value is :"+uid)
        if(uid.isNotEmpty()){
            userProfileController.getUser(uid){ user ->
                if(user != null){
                    Log.i("TheUserIs", "the value is $user")
                    userFullName.text = user.firstName + " " + user.lastName
                    userFirstName.text = user.firstName
                    userLastName.text = user.lastName
                    userEmail.text = user.userEmail
                    if(user.imageUrl.isNullOrEmpty()) {
                        Glide.with(applicationContext)
                            .load(R.drawable.ic_defalut_profile_image)
                            .into(userProfileImage)
                    }
                    else{
                        Glide.with(applicationContext)
                            .load(user.imageUrl)
                            .into(userProfileImage)
                    }
                }
            }
        }
        statsBtn.setOnClickListener {
            val intent = Intent(this@UserProfileActivity, UserStatsActivity::class.java)
            startActivity(intent)
        }
        editBtn.setOnClickListener{
            //set edit variables visibility
            saveBtn.visibility = View.VISIBLE
            editEmail.visibility = View.VISIBLE
            editFirstName.visibility = View.VISIBLE
            editLastName.visibility = View.VISIBLE
            uploadImageBtn.visibility = View.VISIBLE
            //make the user data text view invisible
            userFirstName.visibility = View.GONE
            userLastName.visibility = View.GONE
            userEmail.visibility = View.GONE
            //set the edit text data to the last user data
            editEmail.setText(userEmail.text)
            editFirstName.setText(userFirstName.text)
            editLastName.setText(userLastName.text)



            saveBtn.setOnClickListener {
                //update user profile in database here
                reloadView()
            }
            uploadImageBtn.setOnClickListener{
                //update image url here
                val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(gallery,pickImage)
              //  reloadView()
            }


        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            userProfileController.uploadImageToFirebaseStorage(imageUri,uid){
                url ->
                if(url != null){
                    Log.e("urlvalue", "the url is : $url")
                    val profileImage: CircleImageView = findViewById(R.id.user_profile_image)
                    Glide.with(applicationContext)
                        .load(url)
                        .into(profileImage)
                }
            }

        }
    }



    private fun reloadView() {
        val intent = Intent(this, UserProfileActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


}