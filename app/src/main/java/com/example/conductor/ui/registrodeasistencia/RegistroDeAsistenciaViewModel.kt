package com.example.conductor.ui.registrodeasistencia

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Asistencia
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import com.example.conductor.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroDeAsistenciaViewModel(val app: Application, val dataSource: AppDataSource): BaseViewModel(app) {

    private var _registroDeAsistencia = MutableLiveData<MutableList<Asistencia>>()
    val registroDeAsistencia: LiveData<MutableList<Asistencia>>
        get() = _registroDeAsistencia


    private val _status =MutableLiveData<CloudRequestStatus>()
    val status: LiveData<CloudRequestStatus>
        get() = _status
    suspend fun exportarRegistroDeAsistenciaAExcel(context: Context,desde:String, hasta:String){

        viewModelScope.launch{
            withContext(Dispatchers.Main){
                _status.value = CloudRequestStatus.LOADING
            }
        }
        dataSource.exportarRegistroDeAsistenciaAExcel(context, desde, hasta)

        viewModelScope.launch{
            withContext(Dispatchers.Main){
                _status.value = CloudRequestStatus.DONE
            }
        }
    }

    suspend fun obtenerExcelDelRegistroDeAsistenciaDesdeElBackendYParcearloALista(
        context: Context,desde:String, hasta:String){
        val lista = dataSource.obtenerExcelDelRegistroDeAsistenciaDesdeElBackendYParcearloALista(context, desde, hasta)
        if(lista.isNotEmpty()){
            _registroDeAsistencia.postValue(lista)
        }
    }

    init{
        _status.value = CloudRequestStatus.DONE
    }
}