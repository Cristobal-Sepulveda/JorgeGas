package com.example.conductor.ui.registrodeasistencia

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Asistencia
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import com.example.conductor.ui.base.BaseViewModel
import com.example.conductor.utils.changeUiStatusInMainThread

class RegistroDeAsistenciaViewModel(val app: Application, val dataSource: AppDataSource): BaseViewModel(app) {
    private var _registroDeAsistencia = MutableLiveData<MutableList<Asistencia>>()
    val registroDeAsistencia: LiveData<MutableList<Asistencia>>
        get() = _registroDeAsistencia
    private val _status = MutableLiveData<CloudRequestStatus>()
    val status: LiveData<CloudRequestStatus>
        get() = _status
    suspend fun exportarRegistroDeAsistenciaAExcel(context: Context, mes:String, anio:String){
        this.changeUiStatusInMainThread(_status, CloudRequestStatus.LOADING)
        dataSource.exportarRegistroDeAsistenciaAExcel(context, mes, anio)
        this.changeUiStatusInMainThread(_status, CloudRequestStatus.DONE)
    }
    suspend fun obtenerRegistroDeAsistenciaYMostrarloComoExcel(context: Context,mes:String, anio:String){
        this.changeUiStatusInMainThread(_status, CloudRequestStatus.LOADING)
        val lista = dataSource.obtenerRegistroDeAsistenciaYMostrarloComoExcel(context, mes, anio)
        if(lista.isNotEmpty()){
            _registroDeAsistencia.postValue(lista)
            this.changeUiStatusInMainThread(_status, CloudRequestStatus.DONE)
        }else{
            this.changeUiStatusInMainThread(_status, CloudRequestStatus.ERROR)
        }
    }
    suspend fun agregarBonoPersonalAlVolantero(bono: String, volanteroId: String, mes:String, anio:String){
        this.changeUiStatusInMainThread(_status, CloudRequestStatus.LOADING)
        if(dataSource.agregarBonoPersonalAlVolantero(bono, volanteroId,mes,anio)){
            obtenerRegistroDeAsistenciaYMostrarloComoExcel(app.applicationContext, mes, anio)
        }else{
            this.changeUiStatusInMainThread(_status, CloudRequestStatus.ERROR)
        }
    }
    fun vaciarRecyclerView(){
        _registroDeAsistencia.value = mutableListOf()
    }
    init{
        _status.value = CloudRequestStatus.DONE
    }
}