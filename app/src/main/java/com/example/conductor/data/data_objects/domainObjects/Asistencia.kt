package com.example.conductor.data.data_objects.domainObjects

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Asistencia(
    val idUsuario: String,
    val nombreCompleto: String,
    val sueldoDiario: String,
    val diasTrabajados: String,
    val sueldo: String,
    val bono: String,
    val total: String
) : Parcelable {
    fun longConverterToString(id: Long): String{
        return id.toString()
    }
}