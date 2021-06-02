package com.capstone.ayoperbaiki.utils

import android.view.View
import com.capstone.ayoperbaiki.core.domain.model.Disaster
import com.capstone.ayoperbaiki.core.domain.model.Feedback
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlin.random.Random

object Utils {

    fun View.show() {
        visibility = View.VISIBLE
    }

    fun View.hide() {
        visibility = View.GONE
    }

    val STARTING_COORDINATE = LatLng(-2.44565,117.8888)

}