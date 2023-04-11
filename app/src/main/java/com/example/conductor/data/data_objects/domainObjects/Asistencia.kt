package com.example.conductor.data.data_objects.domainObjects

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Asistencia(
    val fecha: String,
    val ingresoJornada: String?,
    val salidaJornada: String?,
) : Parcelable {
    fun longConverterToString(id: Long): String{
        return id.toString()
    }
}