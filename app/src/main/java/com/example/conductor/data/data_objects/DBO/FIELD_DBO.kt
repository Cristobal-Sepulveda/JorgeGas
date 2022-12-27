package com.example.conductor.data.data_objects.DBO

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Immutable model class & attributes for a Field. In order to compile with Room
 *
 * @param name          name of the field
 * @param address       address of the field
 * @param id            id of the field
 * @param latitude      location GeoPoint of the field
 * @param longitude     longitude GeoPoint of the field
 *
 */

@Entity
data class FIELD_DBO(
    val name: String,
    val address: String,
    val comuna: String,
    val latitude: Double,
    val longitude: Double,
    @PrimaryKey val id: String = UUID.randomUUID().toString()
    )