package com.example.conductor.data.data_objects.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class UsuarioDBO(
    val nombre: String,
    val apellidos: String,
    val rol: String,
    val fotoPerfil:String,
    @PrimaryKey val id: String = UUID.randomUUID().toString()
    )