package com.capstone.ayoperbaiki.core.data.source.firebase.response

import com.capstone.ayoperbaiki.core.domain.model.Feedback
import com.google.firebase.Timestamp


data class ReportResponse(
    val id: Int = 0,
    val disaster: String = "",
    val latitude: Float = 0.0f,
    val longitude: Float = 0.0f,
    val timeStamp: Timestamp = Timestamp.now(),
    val description: String = "",
    val feedback: FeedbackResponse = FeedbackResponse(false, ""),
    val photoUri: String = ""
)
