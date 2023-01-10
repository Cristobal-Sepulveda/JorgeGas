package com.example.conductor.ui.administrarcuentas

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Usuario
import kotlinx.coroutines.launch

class AdministrarCuentasViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    private val _navigateToSelectedUsuario = MutableLiveData<Usuario>()
    val navigateToSelectedUsuario: LiveData<Usuario>
        get() = _navigateToSelectedUsuario

    val usuariosInScreen: MediatorLiveData<List<Usuario>> = MediatorLiveData()

    private val _domainUsuariosInScreen = MutableLiveData<List<Usuario>>()
    val domainUsuariosInScreen: LiveData<List<Usuario>>
        get() = _domainUsuariosInScreen

    /** Theses are for navigate to Detail Fragment **/
    fun displayUsuarioDetails(usuario: Usuario) {
        _navigateToSelectedUsuario.value = usuario
    }

    init{
        viewModelScope.launch{
            _domainUsuariosInScreen.value = dataSource.obtenerUsuariosDesdeFirestore()
            usuariosInScreen.addSource(domainUsuariosInScreen){
                usuariosInScreen.value = it
            }
        }
    }
}