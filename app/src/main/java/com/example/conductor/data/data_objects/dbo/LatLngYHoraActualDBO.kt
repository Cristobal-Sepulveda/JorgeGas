package com.example.conductor.data.data_objects.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class LatLngYHoraActualDBO(
    val lat: Double,
    val lng: Double,
    val hora: String,
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    )