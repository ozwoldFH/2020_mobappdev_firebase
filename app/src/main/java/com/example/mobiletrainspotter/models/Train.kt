package com.example.mobiletrainspotter.models

import java.time.LocalDateTime

data class Train(
    val imageUrls: ArrayList<String>,
    val parts: ArrayList<TrainPart>,
    val location: String,
    val no: String,
    val comment: String,
    val timestamp: LocalDateTime
)