package com.capstone.ayoperbaiki.core.domain.usecase

import android.net.Uri
import com.capstone.ayoperbaiki.core.data.Resource
import com.capstone.ayoperbaiki.core.domain.model.Report
import com.capstone.ayoperbaiki.core.domain.repository.IReportRepository
import javax.inject.Inject

class ReportInteractor @Inject constructor(private val reportRepository: IReportRepository) : ReportUseCase {

    override suspend fun getAllReport(): Resource<List<Report>> = reportRepository.getAllReport()

    override suspend fun submitReport(report: Report) : Resource<Boolean> = reportRepository.submitReport(report)

    override fun uploadImageWithUri(uri: Uri, block: ((Resource<Uri>, Int) -> Unit)?) = reportRepository.uploadImageWithUri(uri, block)


}