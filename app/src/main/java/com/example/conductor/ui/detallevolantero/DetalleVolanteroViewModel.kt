package com.example.conductor.ui.detallevolantero

import android.app.Application
import com.example.conductor.ui.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.network.DistanceMatrixResponse

class DetalleVolanteroViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    suspend fun obtenerRegistroDiariosDelVolantero(id: String): Any {
        return dataSource.obtenerRegistroDiariosDelVolantero(id)
    }


    suspend fun obtenerDistanciaEntreLatLngs(origin: String, destination: String, apiKey: String): DistanceMatrixResponse {
        return dataSource.obtenerDistanciaEntreLatLngs(origin, destination, apiKey)
    }
}