package com.example.conductor.ui.map

import android.app.Application

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.DBO.PERMISSION_DENIED_DBO
import kotlinx.coroutines.launch


enum class CloudDownloadComplete{LOADING, ERROR, DONE}
class MapViewModel(val app: Application, val dataSource: AppDataSource) : BaseViewModel(app) {

    private val _locationPermissionRequests = MutableLiveData<Int>()
    val locationPermissionRequests: LiveData<Int>
        get()= _locationPermissionRequests


    private val _status = MutableLiveData<CloudDownloadComplete>(CloudDownloadComplete.LOADING)
    val status: LiveData<CloudDownloadComplete>
        get()= _status

    suspend fun saveLocationPermissionRequest(){
        viewModelScope.launch {
            val intentosDeObtenerPermisos = dataSource.obtenerIntentoDePermisos()
            if(intentosDeObtenerPermisos.isEmpty()){
                return@launch dataSource.registrarIntentoDeObtenerPermisos(PERMISSION_DENIED_DBO(1))
            }
            val intento = intentosDeObtenerPermisos[0]
            intento.timesDenied = intento.timesDenied +1
            return@launch dataSource.registrarIntentoDeObtenerPermisos(intento)
        }
    }

    suspend fun gettingLocationPermissionRequest(){
        val intentosDeObtenerPermisos = dataSource.obtenerIntentoDePermisos()
        if(intentosDeObtenerPermisos.isEmpty()){
            _locationPermissionRequests.value = 0
        }else{
            _locationPermissionRequests.value = intentosDeObtenerPermisos[0].timesDenied
        }
    }

    init{
        viewModelScope.launch{
            gettingLocationPermissionRequest()
        }
    }
}
