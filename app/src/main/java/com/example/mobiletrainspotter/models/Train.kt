package com.example.mobiletrainspotter.models

import java.time.LocalDateTime

data class Train(
    var imageFilenames: ArrayList<String> = arrayListOf(),
    var parts: ArrayList<TrainPart> = arrayListOf(),
    var location: String = "",
    var no: String = "",
    var comment: String = "",
    var rawTimestamp: String = LocalDateTime.MIN.toString()
) {
    val timestamp: LocalDateTime
        get() = LocalDateTime.parse(rawTimestamp)
}