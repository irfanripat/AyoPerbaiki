package com.capstone.ayoperbaiki.maps

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.capstone.ayoperbaiki.R
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

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
        //hide loading indicator while process is finish
    }

    private fun showLoadingIndicator() {
        //display loading indicator while data is on progress to load
    }

    private fun showErrorMessage() {
        //display some text to user to tell them that data was failed to load
    }

    private fun displayDisasterOnMap(data: List<Report>) {
        mapFragment.getMapAsync {googleMap ->
            data.mapIndexed { index, report ->
                Log.d("REPORT_ACCEPTED", report.id.toString())
                googleMap.addMarker(
                        MarkerOptions()
                                .position(LatLng(report.latitude.toDouble()+index, report.longitude.toDouble()-index))
                                .title(report.disaster+"a")
                                .icon(BitmapDescriptorFactory.fromResource(icon.random()))
                )
            }
        }
            //display icon in map
            //add listener if clicked the bottom sheet will show and load the data
    }

    val icon = arrayOf(R.drawable.ic_banjir, R.drawable.ic_gempa, R.drawable.ic_gunung_berapi, R.drawable.ic_tsunami, R.drawable.ic_longsor, R.drawable.ic_kebakaran)


}