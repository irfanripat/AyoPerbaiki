package com.capstone.ayoperbaiki.maps

import androidx.lifecycle.*
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.core.domain.usecase.ReportUseCase
import com.google.android.gms.maps.model.LatLng
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
        _selectedLatLang.value = null
    }
    fun getAllReport() {
        _listReport.value = Resource.Loading()
        viewModelScope.launch {
            _listReport.postValue(reportUseCase.getAllReport())
        }
    }

    fun setCurrentSelectedLatLng(latLng: LatLng) {
        _selectedLatLang.value = latLng
    }

}