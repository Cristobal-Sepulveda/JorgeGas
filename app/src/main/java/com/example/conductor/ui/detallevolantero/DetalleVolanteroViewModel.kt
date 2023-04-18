package com.example.conductor.ui.detallevolantero

import android.app.Application
import android.content.Context
import com.example.conductor.ui.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.network.DistanceMatrixResponse

class DetalleVolanteroViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    suspend fun obtenerRegistroDiariosDelVolantero(id: String,context: Context): Any {
        return dataSource.obtenerRegistroDiariosDelVolantero(id, context)
    }


    suspend fun obtenerDistanciaEntreLatLngs(origin: String, destination: String, apiKey: String): DistanceMatrixResponse {
        return dataSource.obtenerDistanciaEntreLatLngs(origin, destination, apiKey)
    }
}