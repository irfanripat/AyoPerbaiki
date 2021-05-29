package com.capstone.ayoperbaiki.core.domain.model

data class Report(
    val id: Int,
    val disaster: String,
    val latitude: Float,
    val longitude: Float,
    val timeStamp: String,
    val description: String,
    val feedback: Feedback,
    val photoUri: String
)
