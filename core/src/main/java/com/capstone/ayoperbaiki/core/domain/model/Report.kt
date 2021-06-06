package com.capstone.ayoperbaiki.core.domain.model

import com.google.firebase.Timestamp

data class Report(
    val id: Int,
    val timeStamp: Timestamp,
    val disaster: Disaster,
    val address: Address,
    val description: String,
    val feedback: Feedback,
    val photoUri: String
) {

}
