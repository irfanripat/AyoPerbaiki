package com.capstone.ayoperbaiki.maps

import androidx.lifecycle.ViewModel
import com.capstone.ayoperbaiki.core.domain.usecase.ReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val reportUseCase: ReportUseCase) : ViewModel() {
}