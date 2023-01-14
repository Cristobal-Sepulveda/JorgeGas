package com.example.conductor.ui.administrarcuentas

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdministrarCuentasViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    private val _navigateToSelectedUsuario = MutableLiveData<Usuario?>()
    val navigateToSelectedUsuario: MutableLiveData<Usuario?>
        get() = _navigateToSelectedUsuario

    val usuariosInScreen: MediatorLiveData<List<Usuario>> = MediatorLiveData()

    private val _domainUsuariosInScreen = MutableLiveData<List<Usuario>>()
    val domainUsuariosInScreen: LiveData<List<Usuario>>
        get() = _domainUsuariosInScreen

    /** Theses are for navigate to Detail Fragment **/
    fun displayUsuarioDetails(usuario: Usuario) {
        _navigateToSelectedUsuario.value = usuario
    }
    fun cleanUsuarioDetails() {
        _navigateToSelectedUsuario.value = null
    }

    fun displayUsuariosInRecyclerView(){
        viewModelScope.launch{
            _domainUsuariosInScreen.value = dataSource.obtenerUsuariosDesdeFirestore()
            usuariosInScreen.addSource(domainUsuariosInScreen){
                usuariosInScreen.value = it
            }
        }
    }

    fun removeUsuariosInRecyclerView(){
        usuariosInScreen.removeSource(domainUsuariosInScreen)
    }

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

    fun ingresarUsuarioAFirestore(usuario: Usuario){
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                dataSource.ingresarUsuarioAFirestore(usuario)
            }
        }
    }
    init{

    }

}