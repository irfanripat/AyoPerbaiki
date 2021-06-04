package com.capstone.ayoperbaiki.utils

import android.Manifest
import android.view.View
import com.capstone.ayoperbaiki.core.domain.model.Disaster
import com.capstone.ayoperbaiki.core.domain.model.Feedback
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.form.DisasterReportFormActivity
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlin.random.Random

object Utils {
    val REQUIRED_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    const val EXTRA_DATA_ADDRESS = "extra_data_address"
    const val DATE_PICKER_TAG = "DatePicker"
    const val IMAGE_FILE_FORMAT = "yy-MM-dd-HH-mm-ss-SSS"
    const val PERMISSION_REQUEST_CODE = 100

    fun View.show() {
        visibility = View.VISIBLE
    }

    fun View.hide() {
        visibility = View.GONE
    }

    val STARTING_COORDINATE = LatLng(-2.44565,117.8888)


}