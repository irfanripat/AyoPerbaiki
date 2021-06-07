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
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ReportViewModel @Inject constructor(private val reportUseCase: ReportUseCase): ViewModel() {

    private val _submitReportStatus = MutableLiveData<Resource<Boolean>>()
    private val _uploadImageStatus = MutableLiveData<Resource<Uri>>()
    private val _uploadImagePercentage = MutableLiveData<Int>()
    private val _listPhotoUrl = MutableLiveData<List<String>>()
    private val _listPhotoUri = MutableLiveData<MutableList<Uri>>()

    val submitReportStatus : LiveData<Resource<Boolean>> = _submitReportStatus
    val uploadImageStatus : LiveData<Resource<Uri>> = _uploadImageStatus
    val uploadImagePercentage : LiveData<Int> = _uploadImagePercentage
    val listPhotoUrl : LiveData<List<String>> = _listPhotoUrl
    val listPhotoUri : LiveData<MutableList<Uri>> = _listPhotoUri

    fun submitReport(report: Report)  {
        _submitReportStatus.value = Resource.Loading()
        viewModelScope.launch {
            _submitReportStatus.postValue(reportUseCase.submitReport(report))
        }
    }

    fun uploadImage() {
        listPhotoUri.value?.forEach { uri ->
            _uploadImageStatus.value = Resource.Loading()
            viewModelScope.launch(Dispatchers.IO) {
                reportUseCase.uploadImageWithUri(uri) { resource, i ->
                    _uploadImagePercentage.postValue(i)
                    _uploadImageStatus.postValue(resource)

                    if (resource is Resource.Success)
                        _listPhotoUrl.value?.plus(resource.data)
                }
            }
        }
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