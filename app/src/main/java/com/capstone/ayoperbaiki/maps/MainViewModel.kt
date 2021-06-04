package com.capstone.ayoperbaiki.maps

import android.location.Geocoder
import androidx.lifecycle.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
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



}