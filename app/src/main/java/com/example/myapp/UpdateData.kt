package com.example.myapp


import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UpdateData {
    private lateinit var dbRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth


    fun addToTotalScoreBillboard( value: Int) {
        val user = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance()
        val reference1 = database.getReference("Billboard")

        //  val totalScoreRef = database.getReference("total_score/$userId")

        reference1.child(user.toString()).child("total_score").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentValue = mutableData.value as Long?
                if (currentValue == null) {
                    mutableData.value = value.toLong()
                } else {
                    mutableData.value = currentValue + value
                }
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                databaseError: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                // Handle any errors that may have occurred during the transaction
                if (databaseError != null) {
                    println("Error updating total score: " + databaseError.message)
                }
            }
        })
    }


    fun updateUserScore(newScore: Int){
        val user = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Users")

        if (user != null) {
            reference.child(user).child("personal_score").setValue(newScore)
        }

    }
    fun changePassword(id : String, newPass: Int){
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Users")
        reference.child(id).child("pass").setValue(newPass)
    }



}