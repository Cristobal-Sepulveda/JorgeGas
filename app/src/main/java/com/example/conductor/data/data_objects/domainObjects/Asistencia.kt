package com.example.conductor.data.data_objects.domainObjects

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Asistencia(
    val id: String,
    val fecha: String,
    val horaEntrada: String?,
    val horaSalida: String?,
) : Parcelable {
    fun longConverterToString(id: Long): String{
        return id.toString()
    }
}