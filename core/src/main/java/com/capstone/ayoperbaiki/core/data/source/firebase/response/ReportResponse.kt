package com.capstone.ayoperbaiki.core.data.source.firebase.response

import com.capstone.ayoperbaiki.core.domain.model.Feedback
import com.google.firebase.Timestamp


data class ReportResponse(
    val id: Int = 0,
    val disaster: DisasterResponse = DisasterResponse(-1, ""),
    val address: AddressResponse = AddressResponse("", "", "", "", "", "", 0.0, 0.0),
    val timeStamp: Timestamp = Timestamp.now(),
    val description: String = "",
//    val typeOfDamage: String = "",
    val feedback: FeedbackResponse = FeedbackResponse(false, ""),
    val photoUri: String = ""
)
