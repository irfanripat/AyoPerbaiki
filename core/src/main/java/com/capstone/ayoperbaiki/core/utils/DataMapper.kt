package com.capstone.ayoperbaiki.core.utils

import com.capstone.ayoperbaiki.core.data.source.firebase.response.ReportResponse
import com.capstone.ayoperbaiki.core.domain.model.Feedback
import com.capstone.ayoperbaiki.core.domain.model.Report

object DataMapper {

    fun mapResponseToDomain(input: List<ReportResponse>) : List<Report> {
        val reportList = ArrayList<Report>()
        input.map {
            val report = Report(
                id = it.id,
                disaster = it.disaster,
                latitude = it.latitude,
                longitude = it.longitude,
                timeStamp = it.timeStamp,
                description = it.description,
                feedback = Feedback(it.feedback.status, it.feedback.description),
                photoUri = it.photoUri
            )
            reportList.add(report)
        }
        return reportList
    }

}