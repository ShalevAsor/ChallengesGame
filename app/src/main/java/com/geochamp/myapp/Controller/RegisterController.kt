package com.geochamp.myapp.Controller

import android.content.Intent
import android.widget.Toast
import com.geochamp.myapp.Model.ScoreModel
import com.geochamp.myapp.View.LoginActivity

import com.geochamp.myapp.Model.UserModel
import com.geochamp.myapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

/**
 * RegisterController class
 *
 * This class is used to handle the registration process of a user in the app.
 * It uses the `ActivityRegisterBinding` class to get the references of UI elements,
 * `FirebaseAuth` class to create a new user with email and password,
 * `DatabaseReference` class to store the user data in the Firebase database.
 *
 * @property binding: ActivityRegisterBinding instance to get references of UI elements.
 * @property firebaseAuth: FirebaseAuth instance to create a new user with email and password.
 * @property dbRef: DatabaseReference instance to store user data in the Firebase database.
 * @property dbRef_2: DatabaseReference instance to store user billboard data in the Firebase database.
 *
 * @author [Author Name]
 */
class RegisterController(private val binding: ActivityRegisterBinding,
                         private val firebaseAuth: FirebaseAuth,
                         private val dbRef: DatabaseReference,
                         private val dbRef_2: DatabaseReference) {

    /**
     * This method redirect the user to the login page.
     * It is triggered when the user presses the "Go to Login" button.
     * It starts the `LoginActivity` by creating an Intent and calling the `startActivity()` method.
     */
    fun goToLogin() {
        val intent = Intent(binding.root.context, LoginActivity::class.java)
        binding.root.context.startActivity(intent)
    }
    /**
     * Function to register a user with email and password.
     *
     * This method is triggered when the user presses the "Sign up" button.
     * It creates a new user in the FirebaseAuth instance with the given email and password,
     * then stores the user data in the Firebase database using the `dbRef` and `dbRef_2` instances.
     * It shows a toast message indicating the result of the registration process.
     */

    fun registerUser() {
        val email = binding.registerEmail.text.toString()
        val fName = binding.registerFName.text.toString()
        val lName = binding.registerLName.text.toString()
        val pass = binding.registerPass.text.toString()

        if(email.isNotEmpty() && fName.isNotEmpty() && lName.isNotEmpty() && pass.isNotEmpty()) {
            firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                if (it.isSuccessful) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val fullUser = UserModel(fName, lName, email, null, 0, pass)
                    val scoreModer = ScoreModel(fName, 0, null)

                    if (userId != null) {
                        dbRef.child(userId).setValue(fullUser).addOnCompleteListener {
                            Toast.makeText(binding.root.context, "Data inserted successfully", Toast.LENGTH_LONG).show()
                        }
                    }
                    if (userId != null) {
                        dbRef_2.child(userId).setValue(scoreModer).addOnCompleteListener {}
                    }
                    goToLogin()
                } else {
                    Toast.makeText(binding.root.context, it.exception.toString(), Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(binding.root.context, "empty fields", Toast.LENGTH_LONG).show()
        }
    }
    fun registerUserGoogle(){

    }
}