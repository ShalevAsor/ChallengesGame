package com.geochamp.myapp.Controller

import android.net.Uri
import android.util.Log
import com.geochamp.myapp.Model.UserModel
import com.geochamp.myapp.View.UserProfileActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

/**
 * UserProfileController is a class that handles the logic for user profile activities.
 *
 * @param view an instance of UserProfileActivity that is used to update UI.
 *
 */
class UserProfileController(private val view: UserProfileActivity) {
    /**
     * This method retrieves the user data from Firebase Database with the given user ID.
     *
     * @param userID The ID of the user to retrieve.
     * @param userCallback A callback function that takes in a `UserModel` object. This function is called when the user data is retrieved from the database.
     */

    fun getUser(userID: String, userCallback: (user: UserModel?) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userID)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val user = dataSnapshot.getValue(UserModel::class.java)
                Log.i("user2IsTHE", "Failed to read value.$user")
                userCallback(user)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("values", "Failed to read value.", error.toException())
            }
        })
    }
    /**
     * This method stores the image URL to the user's data in Firebase Database.
     *
     * @param imageUrl The URL of the image to store.
     * @param userID The ID of the user whose data the image URL should be stored in.
     */
    private fun loadImageUrlToDatabase(imageUrl: String,userID:String ) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userID)
        dbRef.child("imageUrl").setValue(imageUrl)
            .addOnSuccessListener {
                Log.d("Firebase", "Image URL added to user data")
                Log.d("Firebase", "the url is : $imageUrl")
                updateImageUrlOnBillboard(imageUrl,userID)
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to add image URL to user data", exception)
            }
    }
    /**
     * This function uploads an image to Firebase Storage and updates the image URL in the user's data in Firebase Database.
     *
     * @param imageUri The `Uri` of the image to upload.
     * @param userID The ID of the user whose data the image URL should be stored in.
     */
     fun uploadImageToFirebaseStorage(imageUri: Uri?,userID:String,urlCallBack :  (url: String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${userID}/image.jpg")
        storageRef.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    loadImageUrlToDatabase(imageUrl,userID)
                    urlCallBack(imageUrl)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to upload image to storage", exception)
            }
    }
/**
 * This method updates the image URL of the user on the billboard in Firebase Database.
 *
 *@param imageUrl The URL of the image to store.
 *@param userID The ID of the user whose data the image URL should be stored in.
 **/
    fun updateImageUrlOnBillboard(imageUrl: String,userID:String){
        val dbRef = FirebaseDatabase.getInstance().getReference("Billboard").child(userID)
        dbRef.child("imageUrl").setValue(imageUrl)
            .addOnSuccessListener {
                Log.d("Firebase", "Image URL added to user data")
                Log.d("Firebase", "the url is : $imageUrl")
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to add image URL to user data", exception)
            }
    }
}