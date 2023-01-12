package com.example.conductor.ui.crearusuario

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CrearUsuarioViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    fun ingresarUsuarioAFirestore(usuario: Usuario){
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                dataSource.ingresarUsuarioAFirestore(usuario)
            }
        }
    }

    init{
        viewModelScope.launch{
            }
    }
}
