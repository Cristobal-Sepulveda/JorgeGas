package com.example.conductor.ui.vistageneral

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.conductor.ui.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.dbo.LatLngYHoraActualDBO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class VistaGeneralViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    var usuarioEstaActivo = false
    var usuarioDesdeSqlite = ""

    private var _botonVolantero = MutableLiveData<Boolean>()
    val botonVolantero: LiveData<Boolean>
        get() = _botonVolantero

    private var _distanciaTotalRecorrida = MutableLiveData<String>()
    val distanciaTotalRecorrida: LiveData<String>
        get() = _distanciaTotalRecorrida

    private var _tiempoTotalRecorridoVerde = MutableLiveData<String>()
    val tiempoTotalRecorridoVerde: LiveData<String>
        get() = _tiempoTotalRecorridoVerde

    private var _tiempoTotalRecorridoAmarillo = MutableLiveData<String>()
    val tiempoTotalRecorridoAmarillo: LiveData<String>
        get() = _tiempoTotalRecorridoAmarillo

    private var _tiempoTotalRecorridoRojo = MutableLiveData<String>()
    val tiempoTotalRecorridoRojo: LiveData<String>
        get() = _tiempoTotalRecorridoRojo

    private var _tiempoTotalRecorridoAzul = MutableLiveData<String>()
    val tiempoTotalRecorridoAzul: LiveData<String>
        get() = _tiempoTotalRecorridoAzul

    private var _tiempoTotalRecorridoRosado = MutableLiveData<String>()
    val tiempoTotalRecorridoRosado: LiveData<String>
        get() = _tiempoTotalRecorridoRosado

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

    fun editarDistanciaTotalRecorrida(distancia: Int ){
        val distanciaKm = distancia / 1000.0
        val distanciaKmString = "%.2fkm".format(distanciaKm)
        _distanciaTotalRecorrida.value = distanciaKmString
    }
    fun editarTiempoTotalRecorrido(tiempo: Float, color: String) {
        val seconds = tiempo.roundToInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val tiempoString = "%02d:%02d".format(minutes, remainingSeconds)

        when (color) {
            "verde" -> _tiempoTotalRecorridoVerde.value = tiempoString
            "amarillo" -> _tiempoTotalRecorridoAmarillo.value = tiempoString
            "rojo" -> _tiempoTotalRecorridoRojo.value = tiempoString
            "azul" -> _tiempoTotalRecorridoAzul.value = tiempoString
            "rosado" -> _tiempoTotalRecorridoRosado.value = tiempoString
        }
    }

    suspend fun guardarLatLngYHoraActualEnRoom(latLngYHoraActualDBO: LatLngYHoraActualDBO){
        dataSource.guardarLatLngYHoraActualEnRoom(latLngYHoraActualDBO)
    }

    suspend fun guardarLatLngYHoraActualEnFirestore(context: Context){
        dataSource.guardarLatLngYHoraActualEnFirestore(context)
    }

}