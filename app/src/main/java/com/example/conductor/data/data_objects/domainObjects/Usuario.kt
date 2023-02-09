package com.example.conductor.data.data_objects.domainObjects

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Usuario(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val telefono: String,
    val usuario: String,
    val password: String,
    var deshabilitada: Boolean,
    val rol: String,
) : Parcelable{
    fun longConverterToString(id: Long): String{
        return id.toString()
    }
}