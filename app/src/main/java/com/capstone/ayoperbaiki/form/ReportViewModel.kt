package com.capstone.ayoperbaiki.form

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.core.domain.usecase.ReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportUseCase: ReportUseCase
): ViewModel() {
    private var _reportResult = MutableLiveData<Report>()

    val reportResult: LiveData<Report> = _reportResult

    fun setResultOfReportForm(report: Report){
        _reportResult.postValue(report)
    }

    fun insertFormReport(): Resource<Boolean> {
        val report = _reportResult.value
        var status: Resource<Boolean> = Resource.Loading()
        if(report != null){
            viewModelScope.launch(Dispatchers.IO){
                status = reportUseCase.submitReport(report)

            }
        }
        return status
    }

    fun uploadImage(uri: List<String>){
        uri.forEach {
            Log.d("DisasterReportForm", "uploadImage: string 1 $it")

            reportUseCase.uploadImageWithUri(it.toUri())
            { status, percent ->
                Log.d("DisasterReportForm", "uploadImage: isi status $status status percent $percent")
            }
        }
    }

}