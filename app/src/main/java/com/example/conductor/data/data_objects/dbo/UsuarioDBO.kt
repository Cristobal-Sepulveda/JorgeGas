package com.example.conductor.data.data_objects.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class UsuarioDBO(
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val rol: String,
    @PrimaryKey val id: String = UUID.randomUUID().toString()
    )