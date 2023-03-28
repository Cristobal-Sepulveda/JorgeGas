package com.example.conductor.ui.administrarcuentas

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.ui.estadoactual.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Usuario
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class CloudRequestStatus{LOADING, ERROR, DONE}
class AdministrarCuentasViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    private val _status =MutableLiveData<CloudRequestStatus>()
    val status: LiveData<CloudRequestStatus>
        get() = _status

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
        _status.value = CloudRequestStatus.LOADING
        Log.d("bindingAdapter", "${status.value}")
        viewModelScope.launch {
            val colRef = dataSource.obtenerUsuariosDesdeFirestore()
            if (colRef.isEmpty()) {
                _status.value = CloudRequestStatus.ERROR
                Log.d("bindingAdapter", "${status.value}")
            } else {
                _domainUsuariosInScreen.value = colRef.sortedWith(compareBy { it.nombre })
                todosLosUsuarios = _domainUsuariosInScreen.value as MutableList<Usuario>
                _status.value = CloudRequestStatus.DONE
                Log.d("bindingAdapter", "${status.value}")
            }
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

    suspend fun ingresarUsuarioAFirestore(usuario: Usuario):Boolean{
        val deferred = CompletableDeferred<Boolean>()
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                deferred.complete(dataSource.ingresarUsuarioAFirestore(usuario))
            }
        }
        return deferred.await()
    }
}