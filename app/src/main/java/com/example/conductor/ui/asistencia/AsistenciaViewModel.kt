package com.example.conductor.ui.asistencia

import android.app.Application
import android.content.Context
import com.example.conductor.data.AppDataSource
import com.example.conductor.ui.estadoactual.base.BaseViewModel

class AsistenciaViewModel(val app : Application, val dataSource: AppDataSource) : BaseViewModel(app) {

    suspend fun obtenerRegistroDeAsistenciaDeUsuario(context: Context, id: String): Boolean {
        return dataSource.obtenerRegistroDeAsistenciaDeUsuario(context, id)
    }

    suspend fun registrarIngresoDeJornada(context: Context): Boolean {
        return dataSource.registrarIngresoDeJornada(context)
    }

    suspend fun registrarSalidaDeJornada(context: Context): Boolean {
        return dataSource.registrarSalidaDeJornada(context)
    }
}