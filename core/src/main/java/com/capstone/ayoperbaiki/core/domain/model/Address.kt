package com.capstone.ayoperbaiki.core.domain.model

data class Address(
        val address: String,
        val city: String,
        val state: String,
        val country: String,
        val postalCode: String,
        val knownName: String,
        val latitude: Double,
        val longitude: Double
)
