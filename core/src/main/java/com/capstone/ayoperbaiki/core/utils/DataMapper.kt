package com.capstone.ayoperbaiki.core.utils

import com.capstone.ayoperbaiki.core.data.source.firebase.response.ReportResponse
import com.capstone.ayoperbaiki.core.domain.model.Address
import com.capstone.ayoperbaiki.core.domain.model.Disaster
import com.capstone.ayoperbaiki.core.domain.model.Feedback
import com.capstone.ayoperbaiki.core.domain.model.Report

object DataMapper {

    fun mapResponseToDomain(input: List<ReportResponse>) : List<Report> {
        val reportList = ArrayList<Report>()
        input.map {
            val report = Report(
                    disaster = Disaster(it.disaster.id, it.disaster.disasterName),
                    address = Address(it.address.address, it.address.city, it.address.state, it.address.country, it.address.latitude, it.address.longitude),
                    timeStamp = it.timeStamp,
                    description = it.description,
                    feedback = Feedback(it.feedback.status, it.feedback.description),
                    photoUri = it.photoUri,
                    typeOfDamage = it.typeOfDamage,
            )
            reportList.add(report)
        }
        return reportList
    }

}