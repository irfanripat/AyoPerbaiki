package com.capstone.ayoperbaiki.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.core.domain.usecase.ReportUseCase
import com.capstone.ayoperbaiki.utils.Utils.dummyData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val reportUseCase: ReportUseCase) : ViewModel() {

    private val _listReport = MutableLiveData<Resource<List<Report>>>()

    val listReport : LiveData<Resource<List<Report>>> = _listReport

    init {
        getAllReport()
    }

    fun getAllReport() {
        _listReport.value = Resource.Loading()
        viewModelScope.launch {
            _listReport.postValue(reportUseCase.getAllReport())
        }
    }

}