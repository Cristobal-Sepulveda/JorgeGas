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
import kotlinx.coroutines.launch

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
            val colRef = dataSource.obtenerRegistroDeAsistencia(context)
            if (colRef.isEmpty()) {
                _status.value = CloudRequestStatus.ERROR
                Log.d("bindingAdapter", "${status.value}")
            } else {
                _domainAsistenciaEnScreen.value = colRef
                _status.value = CloudRequestStatus.DONE
                Log.d("bindingAdapter", "${status.value}")
            }
        }
    }

    suspend fun registrarIngresoDeJornada(context: Context, latitude: Double, longitude: Double): Boolean {
        return dataSource.registrarIngresoDeJornada(context,latitude, longitude)
    }

    suspend fun registrarSalidaDeJornada(context: Context): Boolean {
        return dataSource.registrarSalidaDeJornada(context)
    }
}
