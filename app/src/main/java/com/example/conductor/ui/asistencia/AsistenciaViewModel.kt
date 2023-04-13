package com.example.conductor.ui.asistencia

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Asistencia
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import com.example.conductor.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AsistenciaViewModel(val app : Application, val dataSource: AppDataSource) : BaseViewModel(app) {

    private val _status = MutableLiveData<CloudRequestStatus>()
    val status: LiveData<CloudRequestStatus>
        get() = _status

    private val _domainAsistenciaEnScreen = MutableLiveData<MutableList<Asistencia>>()
    val domainAsistenciaEnScreen: LiveData<MutableList<Asistencia>>
        get() = _domainAsistenciaEnScreen

    fun desplegarAsistenciaEnRecyclerView(context: Context){
        _status.value = CloudRequestStatus.LOADING
        Log.d("bindingAdapter", "${status.value}")

        viewModelScope.launch {
            val colRef = dataSource.obtenerRegistroDeAsistenciaDeUsuario(context)
            if (colRef.isEmpty()) {
                _status.value = CloudRequestStatus.ERROR
                Log.d("bindingAdapter", "${status.value}")
                return@launch
            }
            if(colRef.first().fecha == "error"){
                _status.value = CloudRequestStatus.DONE
            } else {
                _domainAsistenciaEnScreen.value = colRef
                _status.value = CloudRequestStatus.DONE
                Log.d("bindingAdapter", "${status.value}")
            }
        }
    }

    suspend fun registrarIngresoDeJornada(context: Context, latitude: Double, longitude: Double) {
        _status.postValue(CloudRequestStatus.LOADING)
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                if(dataSource.registrarIngresoDeJornada(context,latitude, longitude)){
                    _status.postValue(CloudRequestStatus.DONE)
                }else{
                    _status.postValue(CloudRequestStatus.ERROR)
                }
            }
        }
        return
    }

    suspend fun registrarSalidaDeJornada(context: Context) {
        _status.postValue(CloudRequestStatus.LOADING)
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                if(dataSource.registrarSalidaDeJornada(context)){
                    _status.postValue(CloudRequestStatus.DONE)
                }else{
                    _status.postValue(CloudRequestStatus.ERROR)
                }
            }
        }
        return
    }
}
