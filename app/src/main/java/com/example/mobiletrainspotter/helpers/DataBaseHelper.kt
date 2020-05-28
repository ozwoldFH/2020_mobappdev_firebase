package com.example.mobiletrainspotter.helpers

import com.example.mobiletrainspotter.models.Train
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object DataBaseHelper {
    public fun getTrainsReference(): DatabaseReference? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return FirebaseDatabase.getInstance().reference.child("users").child(user.uid).child("trains")
    }

    public fun addTrain(train: Train): Task<Void>? {
        return getTrainsReference()?.push()?.setValue(train)
    }
}