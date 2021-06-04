package com.capstone.ayoperbaiki.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.domain.model.Feedback
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.core.domain.usecase.ReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ReportViewModel @Inject constructor(private val reportUseCase: ReportUseCase): ViewModel() {

    private val _submitReportStatus = MutableLiveData<Resource<Boolean>>()

    val submitReportStatus : LiveData<Resource<Boolean>> = _submitReportStatus

    fun submitReport(report: Report) {
        viewModelScope.launch {
            reportUseCase.submitReport(report)
        }
    }



}