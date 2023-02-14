package com.example.conductor.ui.detallevolantero

import android.app.Application
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource

class DetalleVolanteroViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    suspend fun obtenerRegistroDelVolantero(id: String): Any {
        return dataSource.obtenerRegistroDelVolantero(id)
    }
}