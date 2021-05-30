package com.capstone.ayoperbaiki.core.data

import android.net.Uri
import com.capstone.ayoperbaiki.core.data.source.firebase.FirebaseDataSource
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.core.domain.repository.IReportRepository
import com.capstone.ayoperbaiki.core.utils.DataMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) : IReportRepository {

    override suspend fun getAllReport(): Resource<List<Report>> {
        return when(val response = firebaseDataSource.getAllReport()) {
            is Resource.Success -> Resource.Success(DataMapper.mapResponseToDomain(response.data))
            is Resource.Failure -> Resource.Failure(response.exception)
            is Resource.Loading -> Resource.Loading()
        }
    }

    override suspend fun submitReport(report: Report): Resource<Boolean> = firebaseDataSource.newReport(report)

    override fun uploadImageWithUri(uri: Uri, block: ((Resource<Uri>, Int) -> Unit)?) = firebaseDataSource.uploadImageWithUri(uri, block)

}