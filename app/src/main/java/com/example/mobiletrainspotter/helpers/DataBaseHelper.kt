package com.example.mobiletrainspotter.helpers

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object DataBaseHelper {
    fun getTrainsReference(user: FirebaseUser): DatabaseReference {
        return FirebaseDatabase.getInstance().reference.child("users").child(user.uid).child("trains")
    }
}