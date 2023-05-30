package com.geochamp.myapp.Model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserDataManager {
    fun getUser(userID: String): UserModel? {
        val user = MutableLiveData<UserModel?>()
        val dbRef = FirebaseDatabase.getInstance().getReference("UserModel").child(userID)
        Log.e("visited1", userID)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userData = dataSnapshot.getValue(UserModel::class.java)
                user.value = userData
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("values", "Failed to read value.", error.toException())
            }
        })
        return user.value
    }
}