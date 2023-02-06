package com.example.conductor.ui.administrarcuentas

import android.app.Application
import androidx.lifecycle.LiveData
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


    private val _domainUsuariosInScreen = MutableLiveData<List<Usuario>>()
    val domainUsuariosInScreen: LiveData<List<Usuario>>
        get() = _domainUsuariosInScreen

    var todosLosUsuarios = mutableListOf<Usuario>()
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
                .sortedWith(compareBy { it.nombre })
            todosLosUsuarios = _domainUsuariosInScreen.value as MutableList<Usuario>
        }
    }

    fun removeUsuariosInRecyclerView(){
        _domainUsuariosInScreen.value = mutableListOf()
    }

    fun filtrarUsuariosEnRecyclerViewPorMenu(rol: String){
        if(rol =="Todos"){
            _domainUsuariosInScreen.value = todosLosUsuarios
            return
        }
        val listaADesplegar = mutableListOf<Usuario>()
        val lista = todosLosUsuarios
        _domainUsuariosInScreen.value = mutableListOf()
        for(usuario in lista){
            if(usuario.rol == rol){
                listaADesplegar.add(usuario)
            }
        }
        _domainUsuariosInScreen.value = listaADesplegar
    }

    fun filtrarUsuariosEnRecyclerViewPorEditText(list: MutableList<Usuario>){
        _domainUsuariosInScreen.value = list
    }

    fun editarUsuarioEnFirestore(usuario: Usuario){
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                dataSource.ingresarUsuarioAFirestore(usuario)
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
}