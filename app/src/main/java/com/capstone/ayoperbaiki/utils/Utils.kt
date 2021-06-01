package com.capstone.ayoperbaiki.utils

import android.view.View
import com.capstone.ayoperbaiki.core.domain.model.Disaster
import com.capstone.ayoperbaiki.core.domain.model.Feedback
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.google.firebase.Timestamp
import kotlin.random.Random

object Utils {

    fun View.show() {
        visibility = View.VISIBLE
    }

    fun View.hide() {
        visibility = View.INVISIBLE
    }

}