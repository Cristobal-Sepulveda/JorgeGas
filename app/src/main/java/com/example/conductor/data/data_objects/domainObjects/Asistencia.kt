package com.example.conductor.data.data_objects.domainObjects

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.DecimalFormat
import java.util.*

@Parcelize
data class Asistencia(
    val idUsuario: String,
    val nombreCompleto: String,
    val sueldoDiario: String,
    val diasTrabajados: String,
    val sueldo: String,
    val bonop: String,
    val bonor: String,
    val total: String
) : Parcelable {
    fun longConverterToString(id: Long): String{
        return id.toString()
    }
    fun getFormatted(string:String): String {
        val formatter = DecimalFormat("$#,###")
        val value = string.toDoubleOrNull() ?: 0.0
        return formatter.format(value)
    }

}