package com.example.conductor.ui.vistageneral

import android.app.Application
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VistaGeneralViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    suspend fun obtenerRolDelUsuarioActual():String{
        return withContext(Dispatchers.IO) {
            return@withContext dataSource.obtenerRolDelUsuarioActual()
        }
    }

}