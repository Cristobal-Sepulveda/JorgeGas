package com.example.conductor.data.data_objects.domainObjects

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Asistencia(
    val idUsuario: String,
    val nombreCompleto: String,
    val sueldoDiario: Int,
    val diasTrabajados: Int,
    val sueldo: Int,
    val bono: Int,
    val total: Int
) : Parcelable {
    fun longConverterToString(id: Long): String{
        return id.toString()
    }
}