package com.capstone.ayoperbaiki.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.domain.model.Address
import com.capstone.ayoperbaiki.core.domain.model.Disaster
import com.capstone.ayoperbaiki.core.domain.model.Feedback
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.core.domain.usecase.ReportUseCase
import com.capstone.ayoperbaiki.utils.Disaster.mapDisaster
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val reportUseCase: ReportUseCase) : ViewModel() {

    private val _listReport = MutableLiveData<Resource<List<Report>>>()
    private val _selectedLatLang = MutableLiveData<LatLng?>()

    val listReport : LiveData<Resource<List<Report>>> = _listReport
    val selectedLatLng : LiveData<LatLng?> = _selectedLatLang

    init {
        getAllReport()
        _selectedLatLang.value = null
    }

    private fun getAllReport() {
        _listReport.value = Resource.Loading()
        viewModelScope.launch {
            _listReport.postValue(reportUseCase.getAllReport())
        }
    }

    fun setCurrentSelectedLatLng(latLng: LatLng) {
        _selectedLatLang.value = latLng
    }

    private fun generateDummyReport() {
        viewModelScope.launch {
            for (i in 1..6) {
                reportUseCase.submitReport(
                        Report(
                                id = i,
                                disaster = Disaster(i, mapDisaster.getValue(i)),
                                address = Address(
                                        address = "Jl. Merpati $i",
                                        city = "Luwu",
                                        state = "Sulawesi Selatan",
                                        country = "Indonesia",
                                        postalCode = "91994",
                                        knownName = "Belopa $i",
                                        latitude = arrayLatLng[i-1].latitude,
                                        longitude = arrayLatLng[i-1].longitude,
                                ),
                                timeStamp = Timestamp.now(),
                                description = "Lorep ipsum dolor armet",
                                feedback = Feedback(false, "Belum ada feedback"),
                                photoUri = "https://firebasestorage.googleapis.com/v0/b/ayoperbaiki.appspot.com/o/064242000_1577883977-IMG-20200101-0053.jpg?alt=media&token=30239432-4463-4673-ac67-53eacf3d69f8"
                        )
                )
            }
        }
    }

    private val arrayLatLng = arrayListOf(
            LatLng(-3.5266503115308727, 119.79263751453715),
            LatLng(-6.823073929202724, 107.29646024499053),
            LatLng(-3.3382121019996123, 136.38827854245358),
            LatLng(0.7286636604540608, 114.0913324906917),
            LatLng(-8.516611567217096, 120.3165747277712),
            LatLng(-2.6235293566197955, 103.60138140639717)
    )

}