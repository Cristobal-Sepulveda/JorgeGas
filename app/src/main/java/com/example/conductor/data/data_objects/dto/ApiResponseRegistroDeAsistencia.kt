package com.example.conductor.data.data_objects.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApiResponseRegistroDeAsistencia(
    val msg: String,
    val horaDeLaResponse: String,
):Parcelable{
    fun longConverterToString(id: Long): String{
        return id.toString()
    }
}