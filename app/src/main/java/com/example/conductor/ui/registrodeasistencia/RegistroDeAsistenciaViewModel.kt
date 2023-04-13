package com.example.conductor.ui.registrodeasistencia

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.conductor.data.AppDataSource
import com.example.conductor.ui.base.BaseViewModel

class RegistroDeAsistenciaViewModel(val app: Application, val dataSource: AppDataSource): BaseViewModel(app) {

    private var _registroDeAsistencia = MutableLiveData<MutableList<Map<*, *>>>()
    val registroDeAsistencia: LiveData<MutableList<Map<*, *>>>
        get() = _registroDeAsistencia
    suspend fun obtenerRegistroDeAsistencia(context: Context){
        _registroDeAsistencia.postValue(dataSource.obtenerRegistroDeAsistencia(context))
    }

    fun obtenerListaDeVolanterosEnElRegistroDeAsistencia(): List<String> {
        return _registroDeAsistencia.value?.map { it["nombreCompleto"] as String } ?: emptyList()
    }

}