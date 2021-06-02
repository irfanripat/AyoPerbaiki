package com.capstone.ayoperbaiki.maps

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
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
import com.capstone.ayoperbaiki.core.domain.model.Address
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.databinding.ActivityMainBinding
import com.capstone.ayoperbaiki.form.DisasterReportFormActivity
import com.capstone.ayoperbaiki.utils.Disaster.mapDisasterIcon
import com.capstone.ayoperbaiki.utils.Utils.EXTRA_ADDRESS
import com.capstone.ayoperbaiki.utils.Utils.STARTING_COORDINATE
import com.capstone.ayoperbaiki.utils.Utils.hide
import com.capstone.ayoperbaiki.utils.Utils.show
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), GoogleMap.OnMapLongClickListener {

    private lateinit var binding : ActivityMainBinding
    private val viewModel : MainViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mapFragment: SupportMapFragment
    private var selectedMarker: Marker? = null
    private var selectedAddress: Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initMapFragment()
        initBottomSheet()
        observeAllReport()
        observeSelectedLatLang()
        binding.btnAdd.setOnClickListener {
            addNewReport(selectedAddress!!)
        }
    }

    private fun initMapFragment() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            it.setOnMapLongClickListener(this)
            it.apply {
                setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MainActivity, R.raw.style_json))
                moveCamera(
                        CameraUpdateFactory.newLatLngZoom(STARTING_COORDINATE, 4.0f)
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

    private fun observeSelectedLatLang() {
        viewModel.selectedLatLng.observe(this, { coordinate ->
            mapFragment.getMapAsync { googleMap ->
                if (coordinate != null) {
                    googleMap.apply {
                        selectedMarker = addMarker(MarkerOptions().position(coordinate).title("Lokasi Pilihan"))
                        animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 10.0f))
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
        Toast.makeText(this, getString(R.string.error_msg), Toast.LENGTH_SHORT).show()
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
    }

    private fun displayDetailDisaster(report: Report) {
        val tvDisasterName = findViewById<TextView>(R.id.tv_disaster_type)
        val tvDisasterAddress = findViewById<TextView>(R.id.tv_disaster_address)
        val tvDisasterLatLng = findViewById<TextView>(R.id.tv_disaster_latlng)
        val tvDisasterDesc = findViewById<TextView>(R.id.tv_disaster_desc)
        val tvDisasterTime = findViewById<TextView>(R.id.tv_disaster_time)
        val tvFeedback = findViewById<TextView>(R.id.tv_disaster_feedback)
        val imgDisaster = findViewById<ImageView>(R.id.image_disaster)

        with(report) {
            tvDisasterName.text = disaster.disasterName
            tvDisasterAddress.text = String.format("${address.city}, ${address.state}")
            tvDisasterLatLng.text = String.format("${address.latitude.roundOffDecimal()}°, ${address.longitude.roundOffDecimal()}°")
            tvDisasterDesc.text = description
            tvDisasterTime.text = getDateTime(timeStamp)
            Glide.with(this@MainActivity)
                    .load(photoUri)
                    .placeholder(R.drawable.default_placeholder)
                    .into(imgDisaster)

            if(feedback.status) {
                tvFeedback.text = feedback.description
            } else {
                tvFeedback.text = getString(R.string.feedback_if_false)
            }
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

    private fun addNewReport(address: Address) {
        val intent = Intent(this, DisasterReportFormActivity::class.java)
        intent.putExtra(EXTRA_ADDRESS, address)
        startActivity(intent)
    }

    private fun getAddressFromLocation(latLng: LatLng) {
        val geocode = Geocoder(this, Locale.getDefault())
            try {
                val result = geocode.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    if (result.size > 0 && result[0] != null && result[0].subLocality != null) {
                        Toast.makeText(this@MainActivity, result[0].subLocality, Toast.LENGTH_SHORT).show()
                        with(result[0]) {
                            selectedAddress = Address(
                                subLocality?:"Unknown",
                                subAdminArea?:"Unknown",
                                adminArea?:"Unknown",
                                countryName?:"Unknown",
                                postalCode?:"Unknown",
                                locality?:"Unknown",
                                latitude,
                                longitude
                            )
                            binding.btnAdd.show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Tidak ada data untuk koordinat ini", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: IOException) {
                Toast.makeText(this@MainActivity, getString(R.string.error_msg), Toast.LENGTH_SHORT).show()
            }
    }

    override fun onMapLongClick(latLng: LatLng) {
        binding.btnAdd.hide()
        if (selectedMarker != null) {
            selectedMarker?.remove()
        }
        if (selectedAddress != null) {
            selectedAddress = null
        }
        getAddressFromLocation(latLng)
        viewModel.setCurrentSelectedLatLng(latLng)
    }

}