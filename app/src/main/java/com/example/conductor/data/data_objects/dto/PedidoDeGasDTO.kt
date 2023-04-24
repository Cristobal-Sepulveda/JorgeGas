package com.example.conductor.data.data_objects.dto

import com.google.firebase.firestore.GeoPoint

data class PedidoDeGasDTO(
    val deptoCliente: String?,
    val blockCliente: String?,
    val idCallCenter: String,
    val nombreCompletoCliente: String,
    val direccionCliente: String,
    val telefonoCliente: String,
    val geoPointCliente: GeoPoint,
    val comentarioCallCenter: String,
    val listPedidoDeGas: Map<String,String>,
)