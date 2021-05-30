package com.capstone.ayoperbaiki.utils

import com.capstone.ayoperbaiki.core.domain.model.Feedback
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.google.firebase.Timestamp
import kotlin.random.Random

object Utils {

    val dummyData = Report(
        id = 1,
        disaster = "Gempa Bumi",
        latitude = 10.0f,
        longitude = 9.0f,
        timeStamp = Timestamp.now(),
        description = "description",
        feedback = Feedback(false, "sdfsdfasf"),
        photoUri = "hehehe"
    )
}