package com.example.conductor.ui.registrovolanteros

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistroVolanterosViewModel(val app: Application, val dataSource: AppDataSource) : BaseViewModel(app){

    private var _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String>
        get() = _selectedDate

    private val _status =MutableLiveData<CloudRequestStatus>()
    val status: LiveData<CloudRequestStatus>
        get() = _status

    private var _selectedVolanteros = MutableLiveData<List<String>>()
    val selectedVolanteros: LiveData<List<String>>
        get() = _selectedVolanteros

    init{
        cambiarStatusCloudRequestStatus(CloudRequestStatus.DONE)
    }
    fun setSelectedDate(date: String){
        _selectedDate.value = date
    }

    suspend fun obtenerTodoElRegistroTrayectoVolanteros(context: Context): Any {
        return dataSource.obtenerTodoElRegistroTrayectoVolanteros(context)
    }

    fun cambiarStatusCloudRequestStatus(status: CloudRequestStatus){
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _status.value = status
            }
        }
    }

    fun limpiarSelectedVolanteros(){
        _selectedVolanteros.value = listOf()
    }

    fun setearSelectedVolanteros(list: List<String>){
        _selectedVolanteros.value = list
    }

}
