package com.capstone.ayoperbaiki.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
        val address: String,
        val city: String,
        val state: String,
        val country: String,
        val latitude: Double,
        val longitude: Double
) : Parcelable
