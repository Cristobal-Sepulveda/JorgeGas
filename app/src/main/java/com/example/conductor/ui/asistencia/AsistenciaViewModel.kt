package com.example.conductor.ui.asistencia

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Asistencia
import com.example.conductor.data.data_objects.domainObjects.AsistenciaIndividual
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import com.example.conductor.ui.base.BaseViewModel
import com.example.conductor.utils.changeUiStatusInMainThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AsistenciaViewModel(val app : Application, val dataSource: AppDataSource) : BaseViewModel(app) {

    private val _status = MutableLiveData<CloudRequestStatus>()
    val status: LiveData<CloudRequestStatus>
        get() = _status

    private val _domainAsistenciaEnScreen = MutableLiveData<MutableList<AsistenciaIndividual>>()
    val domainAsistenciaEnScreen: LiveData<MutableList<AsistenciaIndividual>>
        get() = _domainAsistenciaEnScreen

    fun desplegarAsistenciaEnRecyclerView(){
        this.changeUiStatusInMainThread(_status, CloudRequestStatus.LOADING)
        viewModelScope.launch {
            val colRef = dataSource.obtenerRegistroDeAsistenciaDeUsuario()
            if (colRef.isEmpty()) {
                this@AsistenciaViewModel.changeUiStatusInMainThread(_status, CloudRequestStatus.ERROR)
                return@launch
            }
            if(colRef.first().fecha == "error"){
                this@AsistenciaViewModel.changeUiStatusInMainThread(_status, CloudRequestStatus.ERROR)
            } else {
                _domainAsistenciaEnScreen.value = colRef
                this@AsistenciaViewModel.changeUiStatusInMainThread(_status, CloudRequestStatus.DONE)
            }
        }
    }

    suspend fun registrarIngresoDeJornada(latitude: Double, longitude: Double) {
        this.changeUiStatusInMainThread(_status, CloudRequestStatus.LOADING)
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                if(dataSource.registrarIngresoDeJornada(latitude, longitude)){
                    dataSource.generarInstanciaDeEnvioRegistroDeTrayecto()
                    desplegarAsistenciaEnRecyclerView()
                    this@AsistenciaViewModel.changeUiStatusInMainThread(_status, CloudRequestStatus.DONE)
                }else{
                    this@AsistenciaViewModel.changeUiStatusInMainThread(_status, CloudRequestStatus.ERROR)
                }
            }
        }
    }

    suspend fun registrarSalidaDeJornada() {
        this.changeUiStatusInMainThread(_status, CloudRequestStatus.LOADING)
        viewModelScope.launch(Dispatchers.IO){
            if(dataSource.guardarLatLngYHoraActualEnFirestore()){
                if(dataSource.registrarSalidaDeJornada()){
                    dataSource.eliminarInstanciaDeEnvioRegistroDeTrayecto()
                    desplegarAsistenciaEnRecyclerView()
                    this@AsistenciaViewModel.changeUiStatusInMainThread(_status, CloudRequestStatus.DONE)
                }
            }else{
                if(dataSource.obtenerLatLngYHoraActualesDeRoom().isEmpty()){
                    this@AsistenciaViewModel.changeUiStatusInMainThread(_status, CloudRequestStatus.DONE)
                }else{
                    this@AsistenciaViewModel.changeUiStatusInMainThread(_status, CloudRequestStatus.ERROR)
                }
            }
        }
    }
}
