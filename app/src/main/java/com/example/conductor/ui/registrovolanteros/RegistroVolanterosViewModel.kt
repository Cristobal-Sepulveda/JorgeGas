package com.example.conductor.ui.registrovolanteros

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource

class RegistroVolanterosViewModel(val app: Application, val dataSource: AppDataSource) : BaseViewModel(app){

    private var _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String>
        get() = _selectedDate

    fun setSelectedDate(date: String){
        _selectedDate.value = date
    }

    suspend fun obtenerTodoElRegistroTrayectoVolanteros(): Any {
        return dataSource.obtenerTodoElRegistroTrayectoVolanteros()
    }
}
