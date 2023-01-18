package com.example.conductor.ui.vistageneral

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VistaGeneralViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    private val _rolDelUsuario = MutableLiveData<String>()
    val rolDelUsuario: MutableLiveData<String>
        get() = _rolDelUsuario

    init{
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                try{
                    val rolDelUsuarioActual = dataSource.obtenerRolDelUsuarioActual()
                    _rolDelUsuario.postValue(rolDelUsuarioActual)
                }catch(e: Exception){
                    _rolDelUsuario.value = "Error"
                }
            }
        }
    }

}