package com.capstone.ayoperbaiki.maps

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.capstone.ayoperbaiki.R
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.databinding.ActivityMainBinding
import com.capstone.ayoperbaiki.utils.Disaster.mapDisasterIcon
import com.capstone.ayoperbaiki.utils.Utils.hide
import com.capstone.ayoperbaiki.utils.Utils.show
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val viewModel : MainViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mapFragment: SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initMapFragment()
        initBottomSheet()
        binding.btnAdd.setOnClickListener {
            bottomSheetBehavior.state = STATE_EXPANDED
        }
        observeAllReport()
    }

    private fun initMapFragment() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            it.apply {
                setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MainActivity, R.raw.style_json))
                moveCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(-2.44565,117.8888), 4.0f)
                )
            }
        }
    }

    private fun initBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_detail))
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("SwitchIntDef")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    STATE_EXPANDED -> binding.btnAdd.hide()
                    STATE_COLLAPSED -> binding.btnAdd.show()
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }
        )
    }

    private fun observeAllReport() {
        viewModel.listReport.observe(this, { reports ->
            if (reports != null) {
                when (reports) {
                    is Resource.Success -> {
                        hideLoadingIndicator()
                        displayDisasterOnMap(reports.data)
                    }
                    is Resource.Failure -> {
                        hideLoadingIndicator()
                        showErrorMessage()
                    }
                    is Resource.Loading -> {
                        showLoadingIndicator()
                    }
                }
            }
        })
    }

    private fun hideLoadingIndicator() {
        binding.cvProgress.root.hide()
    }

    private fun showLoadingIndicator() {
        binding.cvProgress.root.show()
    }

    private fun showErrorMessage() {
        Toast.makeText(this, "Gagal mengambil data, pastikan koneksi internet anda tidak bermasalah", Toast.LENGTH_SHORT).show()
    }

    private fun displayDisasterOnMap(data: List<Report>) {
        mapFragment.getMapAsync {googleMap ->
            googleMap.apply {
                if (data.isNullOrEmpty()) {
                    showErrorMessage()
                } else {
                    data.map { report ->
                        addMarker(
                                MarkerOptions()
                                        .position(LatLng(report.address.latitude, report.address.longitude))
                                        .title(report.disaster.disasterName)
                                        .icon(BitmapDescriptorFactory.fromResource(mapDisasterIcon.getValue(report.disaster.id)))
                        )
                    }
                }

                setOnMarkerClickListener {
                    it.position.let { marker ->
                        val clickedReport = data.single { report ->
                            report.address.latitude.equals(marker.latitude) and report.address.longitude.equals(marker.longitude)
                        }
                        displayDetailDisaster(clickedReport)
                    }
                    true
                }
            }
        }

            //add listener if clicked the bottom sheet will show and load the data
    }

    private fun displayDetailDisaster(report: Report) {
        val tvDisasterName = findViewById<TextView>(R.id.tv_disaster_type)
        val tvDisasterAddress = findViewById<TextView>(R.id.tv_disaster_address)
        val tvDisasterLatLng = findViewById<TextView>(R.id.tv_disaster_latlng)
        val tvDisasterDesc = findViewById<TextView>(R.id.tv_disaster_desc)
        val tvDisasterTime = findViewById<TextView>(R.id.tv_disaster_time)
        val imgDisaster = findViewById<ImageView>(R.id.image_disaster)

        with(report) {
            tvDisasterName.text = disaster.disasterName
            tvDisasterAddress.text = String.format("${address.city}, ${address.state}")
            tvDisasterLatLng.text = String.format("${address.latitude.roundOffDecimal()}°, ${address.longitude.roundOffDecimal()}°")
            tvDisasterDesc.text = description
            tvDisasterTime.text = getDateTime(timeStamp)
            Glide.with(this@MainActivity)
                    .load(photoUri)
                    .into(imgDisaster)
        }

        bottomSheetBehavior.state = STATE_EXPANDED
    }

    private fun Double.roundOffDecimal(): Double {
        val df = DecimalFormat("#.####", DecimalFormatSymbols(Locale.ENGLISH))
        df.roundingMode = RoundingMode.CEILING
        return df.format(this).toDouble()
    }

    private fun getDateTime(timestamp: Timestamp): String {
        val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        val netDate = Date(milliseconds)
        return sdf.format(netDate).toString()
    }
}