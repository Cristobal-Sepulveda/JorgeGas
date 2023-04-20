package com.example.conductor.data.data_objects.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class EnvioRegistroDeTrayectoDBO(
    val envioElRegistro: Boolean,
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
)