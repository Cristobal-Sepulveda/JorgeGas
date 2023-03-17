package com.example.conductor.data.data_objects.domainObjects

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VolanteroYRecorrido(
    val id: String,
    val fotoPerfil: String,
    val nombre: String,
    val apellidos: String,
    val telefono: String,
    val usuario: String,
    val password: String,
    var deshabilitada: Boolean,
    var sesionActiva: Boolean,
    val rol: String,
    val tiempoEnVerde: String,
    val tiempoEnAmarillo: String,
    val tiempoEnRojo: String,
    val tiempoEnAzul: String,
    val tiempoEnRosado: String,
    val kilometrosRecorridos: String,
): Parcelable {
    fun longConverterToString(id: Long): String{
        return id.toString()
    }
}
