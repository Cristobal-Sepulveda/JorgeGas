package com.example.conductor.ui.registrodeasistencia

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.data.AppDataSource
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import com.example.conductor.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroDeAsistenciaViewModel(val app: Application, val dataSource: AppDataSource): BaseViewModel(app) {

    private var _registroDeAsistencia = MutableLiveData<MutableList<Map<*, *>>>()
    val registroDeAsistencia: LiveData<MutableList<Map<*, *>>>
        get() = _registroDeAsistencia

    private val _status =MutableLiveData<CloudRequestStatus>()
    val status: LiveData<CloudRequestStatus>
        get() = _status

    suspend fun obtenerRegistroDeAsistencia(context: Context){
        viewModelScope.launch{
            withContext(Dispatchers.Main){
                _status.value = CloudRequestStatus.LOADING
            }
        }
        val registroDeAsistencia = dataSource.obtenerRegistroDeAsistencia(context)
        if(registroDeAsistencia.isEmpty() || registroDeAsistencia.first()["fecha"] =="error"){
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    _status.value = CloudRequestStatus.ERROR
                }
            }
        }else{
            _registroDeAsistencia.postValue(registroDeAsistencia)
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    _status.value = CloudRequestStatus.DONE
                }
            }
        }
    }

    fun obtenerListaDeVolanterosEnElRegistroDeAsistencia(): List<String> {
        return _registroDeAsistencia.value?.map { it["nombreCompleto"] as String } ?: emptyList()
    }

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

}