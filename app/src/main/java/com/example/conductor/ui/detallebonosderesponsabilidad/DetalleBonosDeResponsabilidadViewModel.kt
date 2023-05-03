package com.example.conductor.ui.detallebonosderesponsabilidad

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.conductor.data.AppDataSource
import com.example.conductor.ui.base.BaseViewModel

class DetalleBonosDeResponsabilidadViewModel(val app: Application, val dataSource: AppDataSource,): BaseViewModel(app) {

    private val _montoDelBonoDeResponsabilidad = MutableLiveData<Int>()
    val montoDelBonoDeResponsabilidad: LiveData<Int>
        get() = _montoDelBonoDeResponsabilidad

    suspend fun obtenerMontoDelBonoDeResponsabilidad() {
        val foo = dataSource.obtenerMontoDelBonoDeResponsabilidad()
        if(foo != 0) _montoDelBonoDeResponsabilidad.postValue(foo)
    }
}