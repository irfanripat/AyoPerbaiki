    package com.capstone.ayoperbaiki.core.domain.model


import com.google.firebase.Timestamp

data class Report(
    val disaster: Disaster,
    val address: Address,
    val timeStamp: Timestamp,
    val typeOfDamage: String,
    val description: String,
    val feedback: Feedback,
    val photoUri: List<String>
)
