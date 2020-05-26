package com.example.mobiletrainspotter.models

import java.time.LocalDateTime

data class Train (val imagePath: String, val name: String, val description: String, val comment: String, val datetime: LocalDateTime)