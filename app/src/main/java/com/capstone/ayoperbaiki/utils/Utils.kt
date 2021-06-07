package com.capstone.ayoperbaiki.utils

import android.view.View
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    const val EXTRA_DATA_ADDRESS = "extra_data_address"
    const val IMAGE_FILE_FORMAT = "yy-MM-dd-HH-mm-ss-SSS"

    fun View.show() {
        visibility = View.VISIBLE
    }

    fun View.hide() {
        visibility = View.GONE
    }

    val STARTING_COORDINATE = LatLng(-2.44565,117.8888)

    const val LIMIT_PICTURE = 3

    fun Double.roundOffDecimal(): Double {
        val df = DecimalFormat("#.####", DecimalFormatSymbols(Locale.ENGLISH))
        df.roundingMode = RoundingMode.CEILING
        return df.format(this).toDouble()
    }

    fun getDateTime(timestamp: Timestamp): String {
        val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        val netDate = Date(milliseconds)
        return sdf.format(netDate).toString()
    }

    fun <K, V> getKey(hashMap: Map<K, V>, target: V): K {
        return hashMap.filter { target == it.value }.keys.first()
    }

}