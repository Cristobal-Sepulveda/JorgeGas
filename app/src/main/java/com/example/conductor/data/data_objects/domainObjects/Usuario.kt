package com.example.conductor.data.data_objects.domainObjects

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.Base64

@Parcelize
data class Usuario(
    val id: String,
    val fotoPerfil: String,
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