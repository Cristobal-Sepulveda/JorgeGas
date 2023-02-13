package com.example.conductor.ui.gestiondevolanteros

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.RegistroTrayectoVolantero
import com.example.conductor.data.data_objects.domainObjects.Usuario
import kotlinx.coroutines.launch

class GestionDeVolanterosViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {
    var domainUsuariosActivosInScreenRespaldo = mutableListOf<Usuario>()
    var domainUsuariosInactivosInScreenRespaldo = mutableListOf<Usuario>()

    private val _domainUsuariosInactivosInScreen = MutableLiveData<List<Usuario>>()
    val domainUsuariosInactivosInScreen: LiveData<List<Usuario>>
        get() = _domainUsuariosInactivosInScreen

    private val _domainUsuariosActivosInScreen = MutableLiveData<List<Usuario>>()
    val domainUsuariosActivosInScreen: LiveData<List<Usuario>>
        get() = _domainUsuariosActivosInScreen

    fun displayUsuariosInRecyclerView(){
        viewModelScope.launch{
            try{
                val listaDeUsuarios = dataSource.obtenerUsuariosDesdeFirestore()
                val registroTrayectoVolanteros = dataSource.obtenerRegistroTrayectoVolanteros() as MutableList<RegistroTrayectoVolantero>
                val listaDeUsuariosVolanterosActivos = mutableListOf<Usuario>()
                val listaDeUsuariosVolanterosInactivos = mutableListOf<Usuario>()
                for(usuario in listaDeUsuarios){
                    if(usuario.rol == "Volantero"){
                        for(registro in registroTrayectoVolanteros){
                            if(registro.estaActivo && registro.idVolantero == usuario.id){
                                listaDeUsuariosVolanterosActivos.add(usuario)
                            }
                            if(!registro.estaActivo && registro.idVolantero == usuario.id){
                                listaDeUsuariosVolanterosInactivos.add(usuario)
                            }
                        }
                    }
                }
                listaDeUsuariosVolanterosActivos.sortedWith(compareBy { it.nombre })
                listaDeUsuariosVolanterosInactivos.sortedWith(compareBy { it.nombre })
                domainUsuariosInactivosInScreenRespaldo = listaDeUsuariosVolanterosInactivos
                _domainUsuariosInactivosInScreen.value = listaDeUsuariosVolanterosInactivos
                domainUsuariosActivosInScreenRespaldo = listaDeUsuariosVolanterosActivos
                _domainUsuariosActivosInScreen.value = listaDeUsuariosVolanterosActivos
            }catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun filtrarUsuariosActivosEnRecyclerViewPorEditText(list: MutableList<Usuario>){
        _domainUsuariosActivosInScreen.value = list
    }
    fun filtrarUsuariosInactivosEnRecyclerViewPorEditText(list: MutableList<Usuario>){
        _domainUsuariosInactivosInScreen.value = list
    }
}