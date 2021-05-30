package com.capstone.ayoperbaiki.maps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.ayoperbaiki.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)

    }
}