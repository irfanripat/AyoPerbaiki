package com.capstone.ayoperbaiki.core.domain.model

import com.google.firebase.Timestamp

data class Report(
    val id: Int,
    val disaster: String,
    val latitude: Float,
    val longitude: Float,
    val timeStamp: Timestamp,
    val description: String,
    val feedback: Feedback,
    val photoUri: String
) {

}
