package com.example.conductor.ui.editarusuario

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditarUsuarioViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    fun editarUsuarioEnFirestore(usuario: Usuario){
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                dataSource.ingresarUsuarioAFirestore(usuario)
            }
        }
    }

    fun eliminarUsuarioDeFirebase(usuario: Usuario) {
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                usuario.deshabilitada = true
                dataSource.eliminarUsuarioDeFirebase(usuario)
            }
        }
    }

    init{
        viewModelScope.launch{
            }
    }
}
