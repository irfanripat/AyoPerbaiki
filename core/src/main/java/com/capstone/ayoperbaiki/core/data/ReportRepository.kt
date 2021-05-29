package com.capstone.ayoperbaiki.core.data

import android.net.Uri
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.data.source.firebase.FirebaseDataSource
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.core.domain.repository.IReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) : IReportRepository {

    override suspend fun getAllReport(): Resource<List<Report>> = firebaseDataSource.getAllReport()

    override suspend fun submitReport(report: Report): Resource<Boolean> = firebaseDataSource.newReport(report)

    override fun uploadImageWithUri(uri: Uri, block: ((Resource<Uri>, Int) -> Unit)?) = firebaseDataSource.uploadImageWithUri(uri, block)

}