package com.capstone.ayoperbaiki.core.data.source.firebase.response

import com.google.firebase.Timestamp


data class ReportResponse(
    val disaster: DisasterResponse = DisasterResponse(-1, ""),
    val address: AddressResponse = AddressResponse("", "", "", "", "", 0.0, 0.0),
    val timeStamp: Timestamp = Timestamp.now(),
    val description: String = "",
    val typeOfDamage: String = "",
    val feedback: FeedbackResponse = FeedbackResponse(false, ""),
    val photoUri: List<String> = listOf()
)
