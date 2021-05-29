package com.capstone.ayoperbaiki.core.domain.repository


import android.net.Uri
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.domain.model.Report
import kotlinx.coroutines.flow.Flow

interface IReportRepository {

    suspend fun getAllReport() : Resource<List<Report>>

    suspend fun submitReport(report: Report) : Resource<Boolean>

    fun uploadImageWithUri(uri: Uri, block: ((Resource<Uri>, Int) -> Unit)?)

}