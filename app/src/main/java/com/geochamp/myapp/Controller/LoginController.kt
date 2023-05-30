package com.geochamp.myapp.Controller

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.geochamp.myapp.Model.ScoreModel
import com.geochamp.myapp.Model.UserModel
import com.geochamp.myapp.R
import com.geochamp.myapp.View.LoginActivity
import com.geochamp.myapp.View.MapsActivity
import com.geochamp.myapp.View.RegisterActivity
import com.geochamp.myapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

/**
 * Class LoginController is a class that handles the functionality of the Login page.
 * It is responsible for handling the user authentication using email and password
 * or using Google Sign-In.
 *
 * @property activity the LoginActivity instance
 * @property binding the ActivityLoginBinding instance
 * @property firebaseAuth the FirebaseAuth instance
 * @property mGoogleSignInClient the GoogleSignInClient instance
 */

class LoginController(private val activity: LoginActivity, private val binding: ActivityLoginBinding, private val firebaseAuth: FirebaseAuth, private val mGoogleSignInClient: GoogleSignInClient) {


    /**
     * Initializes the onClickListeners for the buttons in the LoginActivity and requests the location permission.
     */

    fun onCreate() {
        requestLocationPermission()




    }

    /**
     * If a user is already signed in, either with email/password or Google Sign In, updates the UI to the map activity.
     */

    fun onStart() {


        if (firebaseAuth.currentUser != null || GoogleSignIn.getLastSignedInAccount(activity) != null) {
            updateUI(activity, "map")

        }
        binding.registerPage.setOnClickListener {
            updateUI(activity, "register")
        }
        binding.btnLogin.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val pass = binding.loginPass.text.toString()
            if(email.isEmpty() || pass.isEmpty()){
                Toast.makeText(
                    activity,
                    "Please enter a valid email address or password",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else{
                loginWithEmailAndPassword(email, pass)
            }


        }
        binding.signInButton.setOnClickListener {
            signInWithGoogle()
        }
        binding.forgotpass.setOnClickListener {
            val forgotPassDialog = Dialog(activity)
            forgotPassDialog.setContentView(R.layout.reset_pass_dialog)
            val btnCloseDialog = forgotPassDialog.findViewById<TextView>(R.id.closePopup)
            val btnResetPass = forgotPassDialog.findViewById<Button>(R.id.reset_password)

            btnCloseDialog.setOnClickListener {
                forgotPassDialog.dismiss()
            }

            btnResetPass.setOnClickListener {
                val email =
                    forgotPassDialog.findViewById<EditText>(R.id.reset_pass_email).text.toString()
                if (email.isEmpty()) {
                    Toast.makeText(
                        activity,
                        "Please enter a valid email address",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                activity,
                                "Password reset email sent",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                activity,
                                "Failed to send password reset email: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

            }
            forgotPassDialog.show()
        }
    }

    /**
     * Handles the result of the Google Sign In activity.
     *
     * @param requestCode: request code passed to the activity result
     * @param resultCode: result code returned from the activity result
     * @param data: data passed back from the activity result
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    /**
     * Starts the Google Sign In activity.
     */
    private fun signInWithGoogle() {
        val intent = mGoogleSignInClient.signInIntent
        activity.startActivityForResult(intent, 1)
    }

    /**
     * Handles the result of the Google Sign In activity.
     * If the user is already signed in, signs in with email and password.
     * If not, creates a new user with email and password.
     *
     * @param completedTask: task containing the result of the Google Sign In activity
     */
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            Log.i("visitedgoogle","visited")
            val account = completedTask.getResult(ApiException::class.java)
            val email = account.email as String
            val lastName = account.familyName as String
            val firstName = account.givenName as String
            // TODO: Replace the hardcoded password with a strong password generator
            val password = "123456"
            Log.i("datatest","$lastName")
            // if the user is exist , sign in with email and password
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("visitedgoogle2","visited")
                        updateUI(activity, "map")
                    } else {
                        Log.i("visitedgoogle3","visited")
                        createUserWithEmailAndPassword(email, password, firstName, lastName)
                    }
                }
        } catch (e: ApiException) {
            Log.i("visitedgoogle4","visited")
            Log.e("LoginController", "handleSignInResult: ${e.message}")
            val statusCode = e.statusCode
            Log.e("LoginController", "handleSignInResult: Error $statusCode")

        }
    }

    /**
     * This method updates the UI based on the activity name passed as the argument.
     *
     * @param context The context of the current activity.
     * @param activity The name of the activity to navigate to.
     */
    fun updateUI(context: Context, activity: String) {
        if (activity == "register") {
            val intent = Intent(context, RegisterActivity::class.java)
            context.startActivity(intent)
        }
        if (activity == "login") {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
        if (activity == "map") {
            val intent = Intent(context, MapsActivity::class.java)
            context.startActivity(intent)
        }
    }

    /**
     * This method requests the location permission.
     */
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }
    }

    /**
     * This method logs in the user with the given email and password.
     *
     * @param email The email of the user.
     * @param password The password of the user.
     */
    private fun loginWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateUI(activity, "map")
                } else {
                    Toast.makeText(activity, "Error: ${task.exception?.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    /**
     * This method creates a new user with the given email and password.
     *
     * @param email The email of the user.
     * @param password The password of the user.
     */
    private fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) {

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createUserInDatabase(email, password, firstName, lastName)
                    updateUI(activity, "map")
                } else {
                    Toast.makeText(activity, "Error: ${task.exception?.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    private fun createUserInDatabase(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val fullUser = UserModel(firstName, lastName, email, null, 0, password, 0, 0, 0, 0)

        val scoreModer = ScoreModel(firstName, 0, null)
        val dbRef_users = FirebaseDatabase.getInstance().getReference("Users")
        //dbRef_users.child(userId).child("My_Markers").child(userId).setValue(0)
        val dbRef_billboard = FirebaseDatabase.getInstance().getReference("Billboard")

        if (userId != null) {
            dbRef_users.child(userId).setValue(fullUser).addOnCompleteListener {}
            dbRef_billboard.child(userId).setValue(scoreModer).addOnCompleteListener {}
        }
    }

    private fun generatePassword(length: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf(
            '!',
            '@',
            '#',
            '$',
            '%',
            '^',
            '&',
            '*',
            '(',
            ')',
            '-',
            '_',
            '=',
            '+',
            '[',
            ']',
            '{',
            '}',
            '<',
            '>',
            '?',
            '/'
        )
        val random = Random()
        return (1..length)
            .map { random.nextInt(charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    private fun getUserPassword(userID: String, userCallback: (userPassword: String?) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userID)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val user = dataSnapshot.getValue(UserModel::class.java)
                Log.i("user2IsTHE", "Failed to read value.$user")
                userCallback(user?.pass)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("values", "Failed to read value.", error.toException())
            }
        })
    }

//    fun getGameTimer(userId: String, callback: CallBack2) {
//        val ref = FirebaseDatabase.getInstance().getReference("Users").child(userId)
//
//        ref.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val score = dataSnapshot.getValue(UserModel::class.java)
//                var temp: Long = 0
//                if (score != null) {
//                    temp = score.timePlayed!!
//                }
//                //  val score = dataSnapshot.value as? Long
//                // val scoreModel = scoreSnapshot.getValue(ScoreModel::class.java)
//                callback.onSuccess(temp ?: 0)
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                callback.onSuccess(0)
//            }
//        })
//
//
//    }
//    fun updateTotalTimer(userId: String, elapsedTime: Long) {
//        val database = FirebaseDatabase.getInstance()
//        val reference = database.getReference("Users").child(userId).child("timePlayedToday")
//
//        reference.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if (dataSnapshot.exists()) {
//
//                    val currentScore = 0
//
//                    reference.setValue(elapsedTime)
//                    if(currentScore==0){
//                        reference.setValue(elapsedTime)
//                    }
//                } else {
//                    reference.setValue(elapsedTime)
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Handle error
//            }
//        })
//    }
}


