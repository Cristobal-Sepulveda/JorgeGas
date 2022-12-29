package com.example.conductor.data.data_objects.DBO

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class PERMISSION_DENIED_DBO(
    val timesDenied: Int,
    @PrimaryKey val id: String = UUID.randomUUID().toString()
    )