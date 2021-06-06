package com.capstone.ayoperbaiki.utils

import com.capstone.ayoperbaiki.R

object Disaster {

    val mapDisaster : Map<Int, String> = mapOf(
            1 to "Banjir",
            2 to "Kebakaran Hutan",
            3 to "Gempa Bumi",
            4 to "Tsunami",
            5 to "Tanah Longsor",
            6 to "Gunung Meletus",
            7 to "Lainnya"
    )

    fun generateDisaster(): List<String>{
       return ArrayList<String>().apply {
           mapDisaster.forEach {
               this.add(it.value)
           }
       }
    }

    val mapTypeOfDamage = mapOf(
            1 to "Jaringan Listrik",
            2 to "Jalan Raya",
            3 to "Jembatan",
            4 to "Jalur Kereta Api",
            5 to "Bandara",
            6 to "Pelabuhan",
            7 to "Terminal",
            8 to "Pasar",
            9 to "Sekolah",
            10 to "Stadion",
            11 to "Puskesmas",
            12 to "Bendungan",
            13 to "Lainnya"
    )

    fun generateListTypeOfDamage(): List<String>{
       return ArrayList<String>().apply {
           mapTypeOfDamage.forEach {
               this.add(it.value)
           }
       }
    }

    val mapDisasterIcon = mapOf(
            1 to R.drawable.ic_banjir,
            2 to R.drawable.ic_kebakaran,
            3 to R.drawable.ic_gempa,
            4 to R.drawable.ic_tsunami,
            5 to R.drawable.ic_longsor,
            6 to R.drawable.ic_gunung_berapi
    )

}