package com.example.conductor.data.data_objects.domainObjects

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class RegistroTrayectoVolantero(
    val idVolantero: String,
    val estaActivo: Boolean
) : Parcelable {

}

