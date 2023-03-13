package com.example.conductor.ui.gestiondevolanteros

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.RegistroTrayectoVolantero
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import kotlinx.coroutines.launch

class GestionDeVolanterosViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    private val _status =MutableLiveData<CloudRequestStatus>()
    val status: LiveData<CloudRequestStatus>
        get() = _status

    private val _domainUsuariosInScreen = MutableLiveData<List<Usuario>>()
    val domainUsuariosInScreen: LiveData<List<Usuario>>
        get() = _domainUsuariosInScreen

    var domainUsuariosVolanterosActivos: List<Usuario> = listOf()

    var domainUsuariosVolanterosInactivos: List<Usuario> = listOf()

    private var _volanterosActivos = MutableLiveData<Boolean>()
    val volanterosActivos: LiveData<Boolean>
        get() = _volanterosActivos

    fun displayUsuariosInRecyclerView(){
        viewModelScope.launch{
            try{
                val listaDeUsuariosVolanteros = mutableListOf<Usuario>()
                val listaDeUsuariosVolanterosActivos = mutableListOf<Usuario>()
                val listaDeUsuariosVolanterosInactivos = mutableListOf<Usuario>()
                val listaDeUsuarios = dataSource.obtenerUsuariosDesdeFirestore()
                val registroTrayectoVolanteros = dataSource.obtenerRegistroTrayectoVolanteros() as MutableList<RegistroTrayectoVolantero>

                if(listaDeUsuarios.isEmpty() || registroTrayectoVolanteros.toString() == "Error"){
                    _status.value = CloudRequestStatus.ERROR
                    return@launch
                }

                for(usuario in listaDeUsuarios){
                    if(usuario.rol == "Volantero"){
                        for(registro in registroTrayectoVolanteros){
                            if(registro.idVolantero == usuario.id && registro.estaActivo){
                                listaDeUsuariosVolanteros.add(usuario)
                                listaDeUsuariosVolanterosActivos.add(usuario)
                            }
                            if(registro.idVolantero == usuario.id && !registro.estaActivo){
                                listaDeUsuariosVolanteros.add(usuario)
                                listaDeUsuariosVolanterosInactivos.add(usuario)
                            }
                        }
                    }
                }

                listaDeUsuariosVolanteros.sortedWith(compareBy { it.nombre })
                listaDeUsuariosVolanterosActivos.sortedWith(compareBy { it.nombre })
                listaDeUsuariosVolanterosInactivos.sortedWith(compareBy { it.nombre })

                domainUsuariosVolanterosActivos = listaDeUsuariosVolanterosActivos
                domainUsuariosVolanterosInactivos = listaDeUsuariosVolanterosInactivos
                if(listaDeUsuariosVolanterosActivos.isEmpty()){
                    noHayVolanterosActivos(true)
                }
                _domainUsuariosInScreen.value = listaDeUsuariosVolanterosActivos

                _status.value = CloudRequestStatus.DONE
            }catch(e: Exception) {
                e.printStackTrace()
                _status.value = CloudRequestStatus.ERROR
            }
        }
    }

    fun filtrarUsuariosActivosEnRecyclerViewPorSwitch(){
        _domainUsuariosInScreen.value = domainUsuariosVolanterosActivos
    }
    fun filtrarUsuariosInactivosEnRecyclerViewPorSwitch(){
        _domainUsuariosInScreen.value = domainUsuariosVolanterosInactivos
    }

    fun filtrarUsuariosActivosEnRecyclerViewPorEditText(lista: MutableList<Usuario>){
        _domainUsuariosInScreen.value = lista
    }

    fun filtrarUsuariosInactivosEnRecyclerViewPorEditText(lista: MutableList<Usuario>){
        _domainUsuariosInScreen.value = lista
    }

    fun noHayVolanterosActivos(boolean: Boolean){
        _volanterosActivos.value = boolean
    }
}