package com.example.mobiletrainspotter.models

import com.google.firebase.database.Exclude
import java.io.Serializable
import java.time.LocalDateTime

data class Train(
    var imageFilenames: ArrayList<String> = arrayListOf(),
    var parts: ArrayList<TrainPart> = arrayListOf(),
    var location: String = "",
    var no: String = "",
    var comment: String = "",
    @get:Exclude var timestamp: LocalDateTime = LocalDateTime.MIN
) : Serializable {
    // Only needed to store the timestamp in the database, because parsing LocalDateTime does not work
    var rawTimestamp: String
        get() = timestamp.toString()
        set(value) {
            timestamp = LocalDateTime.parse(value)
        }
}