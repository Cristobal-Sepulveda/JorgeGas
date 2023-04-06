package com.example.conductor.ui.map

import android.app.Application
import android.util.Log

import com.example.conductor.ui.base.BaseViewModel
import com.example.conductor.data.AppDataSource


enum class CloudDownloadComplete{LOADING, ERROR, DONE}
class MapViewModel(val app: Application, val dataSource: AppDataSource) : BaseViewModel(app) {


    var usuarioEstaActivo = false
    var usuarioDesdeSqlite = ""
    var usuarioRolDesdeSqlite = ""



    suspend fun editarEstadoVolantero(estaActivo: Boolean):Boolean{
        if(dataSource.editarEstadoVolantero(estaActivo)){
            usuarioEstaActivo = estaActivo
            return true
        }
        return false
    }

    suspend fun obtenerUsuariosDesdeSqlite() {
        val usuario = dataSource.obtenerUsuariosDesdeSqlite()
        if(usuario.isNotEmpty()){
            Log.i("VistaGeneralViewModel", "$usuario")
            Log.i("VistaGeneralViewModel", "isNotEmpty")
            usuarioDesdeSqlite = "${usuario.first().nombre} ${usuario.first().apellidos}"
            usuarioRolDesdeSqlite = usuario.first().rol
        }else{
            Log.i("VistaGeneralViewModel", "$usuario")
            Log.i("VistaGeneralViewModel", "isEmpty")
        }
    }

}
