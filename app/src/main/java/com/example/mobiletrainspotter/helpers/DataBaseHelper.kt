package com.example.mobiletrainspotter.helpers

import com.example.mobiletrainspotter.models.Train
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object DataBaseHelper {
    fun getTrainsReference(): DatabaseReference? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return FirebaseDatabase.getInstance().reference.child("users").child(user.uid).child("trains")
    }

    fun addTrain(train: Train): Task<Void>? {
        return getTrainsReference()?.push()?.setValue(train)
    }

    fun getTrainReference(trainId: String): DatabaseReference? {
        return getTrainsReference()?.child(trainId)
    }

    fun setTrain(trainId: String, train: Train): Task<Void>? {
        return getTrainReference(trainId)?.setValue(train)
    }

    fun deleteTrain(trainId: String): Task<Void>? {
        return getTrainReference(trainId)?.removeValue()
    }
}