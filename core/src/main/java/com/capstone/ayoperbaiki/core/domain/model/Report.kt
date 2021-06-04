package com.capstone.ayoperbaiki.core.domain.model

import android.net.Uri
import com.google.firebase.Timestamp
import java.util.*

data class Report(
    val timeStamp: Timestamp,
    val disaster: Disaster,
    val typeOfDamage: String,
    val address: Address,
    val description: String,
    val feedback: Feedback,
    val photoUri: List<String>
) {

}
