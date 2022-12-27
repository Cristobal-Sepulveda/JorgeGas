package com.example.conductor.data.data_objects.DTO

import androidx.room.Entity
import com.example.conductor.data.data_objects.DBO.FIELD_DBO

@Entity
data class FIELD_DTO(
    val name: String,
    val address: String,
    val comuna: String,
    val latitude: Double,
    val longitude: Double,
    val id: String
)


fun FIELD_DTO.asDataBaseModel(fieldDTO: FIELD_DTO): FIELD_DBO {
    return FIELD_DBO(
        name = fieldDTO.name,
        address = fieldDTO.address,
        comuna = fieldDTO.comuna,
        latitude = fieldDTO.latitude,
        longitude = fieldDTO.longitude,
        id = fieldDTO.id
    )
}