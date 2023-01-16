package com.example.myapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.myapp.Model.Users
import com.example.myapp.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding:ActivityUserProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var user: Users
    private lateinit var uid:String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_user_profile)
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
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
        val uploadImageBtn = findViewById<ImageView>(R.id.ic_edit_profile_upload_picture)
        editEmail.visibility = View.GONE
        editFirstName.visibility = View.GONE
        editLastName.visibility = View.GONE
        saveBtn.visibility = View.GONE
        uploadImageBtn.visibility = View.GONE

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        Log.i("uid", "the value is :"+uid)
        if(uid.isNotEmpty()){
            databaseReference.child(uid).addValueEventListener(object:ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(Users::class.java)!!
                    userFullName.text = user.firstName + " " + user.lastName
                    userFirstName.text = user.firstName
                    userLastName.text = user.lastName
                    userEmail.text = user.userEmail
                    if(user.imageUrl != " ") {
                        Picasso.get()
                            .load(user.imageUrl)
                            .into(userProfileImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("firebase", "Error getting data")
                }

            })
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
                reloadView()
            }


        }

    }

    private fun reloadView() {
        val intent = Intent(this, UserProfileActivity::class.java)
        startActivity(intent)
        finish()
    }

}