package com.capstone.ayoperbaiki.form

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.core.domain.usecase.ReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class ReportViewModel @Inject constructor(private val reportUseCase: ReportUseCase): ViewModel() {

    private val _submitReportStatus = MutableLiveData<Resource<Boolean>>()
    private val _uploadSingleImageStatus = MutableLiveData<Resource<Uri>>()
//    private val _uploadSingleImageStatus = MutableLiveData<List<Any>>()
    private val _uploadImagePercentage = MutableLiveData<Int>()
    private val _listPhotoUrl = MutableLiveData<MutableList<String>>()
    private val _listPhotoUri = MutableLiveData<MutableList<Uri>>()
    private val _report = MutableLiveData<Report>()

    val submitReportStatus : LiveData<Resource<Boolean>> = _submitReportStatus
    val uploadSingleImageStatus : LiveData<Resource<Uri>> = _uploadSingleImageStatus
//    val uploadSingleImageStatus : LiveData<List<Any>> = _uploadSingleImageStatus
    val uploadImagePercentage : LiveData<Int> = _uploadImagePercentage
    val listPhotoUrl : LiveData<MutableList<String>> = _listPhotoUrl
    val listPhotoUri : LiveData<MutableList<Uri>> = _listPhotoUri

    fun submitReport() {
        _submitReportStatus.value = Resource.Loading()
        viewModelScope.launch {
            _report.value.let {
                val report = Report(
                        disaster = it!!.disaster,
                        timeStamp = it.timeStamp,
                        address = it.address,
                        typeOfDamage = it.typeOfDamage,
                        description = it.description,
                        feedback = it.feedback,
                        photoUri = _listPhotoUrl.value!!.toList()
                )
                _submitReportStatus.postValue(reportUseCase.submitReport(report))
            }
        }
    }

    fun uploadImage() {
        listPhotoUri.value?.forEach { uri ->
            _uploadSingleImageStatus.value = Resource.Loading()
            viewModelScope.launch(Dispatchers.IO) {
                reportUseCase.uploadImageWithUri(uri) { resource, i ->
                    _uploadImagePercentage.postValue(i)
                    _uploadSingleImageStatus.postValue(resource)

                    if (resource is Resource.Success)
                        _listPhotoUrl.plusAssign(resource.data.toString())
                }
            }
        }
    }

    fun addReport(report: Report) {
        _report.value = report
    }

    fun addPhotoUri(uri: Uri) {
        _listPhotoUri.plusAssign(uri)
    }

    fun removePhotoUri(position: Int) {
        _listPhotoUri.minus(position)
    }

    operator fun <T> MutableLiveData<MutableList<T>>.plusAssign(item: T) {
        val value = this.value ?: mutableListOf()
        value.add(item)
        this.value = value
    }

    operator fun <T> MutableLiveData<MutableList<T>>.minus(item: Int) {
        val value = this.value ?: mutableListOf()
        value.removeAt(item)
        this.value = value
    }

}