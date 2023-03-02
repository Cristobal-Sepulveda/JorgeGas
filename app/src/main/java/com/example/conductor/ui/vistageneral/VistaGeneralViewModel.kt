package com.example.conductor.ui.vistageneral

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.dbo.UsuarioDBO
import com.example.conductor.ui.map.CloudDownloadComplete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VistaGeneralViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    var usuarioEstaActivo = false
    var usuarioDesdeSqlite = ""

    private var _botonVolantero = MutableLiveData<Boolean>()
    val botonVolantero: LiveData<Boolean>
        get() = _botonVolantero

    private var _distanciaTotalRecorrida = MutableLiveData<String>()
    val distanciaTotalRecorrida: LiveData<String>
        get() = _distanciaTotalRecorrida

    suspend fun obtenerRolDelUsuarioActual():String{
        return withContext(Dispatchers.IO) {
            return@withContext dataSource.obtenerRolDelUsuarioActual()
        }
    }

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
        }else{
            Log.i("VistaGeneralViewModel", "$usuario")
            Log.i("VistaGeneralViewModel", "isEmpty")
        }
    }

    fun editarBotonVolantero(trackingLocation: Boolean){
        _botonVolantero.value = trackingLocation
    }

    fun editarDistanciaTotalRecorrida(distancia: String){
        _distanciaTotalRecorrida.value = distancia
    }
}