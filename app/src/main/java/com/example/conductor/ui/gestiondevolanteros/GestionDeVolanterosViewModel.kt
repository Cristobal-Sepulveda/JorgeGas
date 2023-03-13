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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GestionDeVolanterosViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    private val _volanterosActivos = MutableLiveData<Boolean>()
    val volanterosActivos: LiveData<Boolean>
        get() = _volanterosActivos

    private val _status =MutableLiveData<CloudRequestStatus>()
    val status: LiveData<CloudRequestStatus>
        get() = _status

    var domainUsuariosActivosInScreenRespaldo = mutableListOf<Usuario>()
    var domainUsuariosInactivosInScreenRespaldo = mutableListOf<Usuario>()

    private val _domainUsuariosInactivosInScreen = MutableLiveData<List<Usuario>>()
    val domainUsuariosInactivosInScreen: LiveData<List<Usuario>>
        get() = _domainUsuariosInactivosInScreen

    private val _domainUsuariosActivosInScreen = MutableLiveData<List<Usuario>>()
    val domainUsuariosActivosInScreen: LiveData<List<Usuario>>
        get() = _domainUsuariosActivosInScreen

    fun removerTextDeInteres(aux: Boolean){
        _volanterosActivos.value = aux
    }


    fun displayUsuariosInRecyclerView(){
        viewModelScope.launch{
            try{

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
                domainUsuariosActivosInScreenRespaldo = listaDeUsuariosVolanterosActivos
                _domainUsuariosInactivosInScreen.value = listaDeUsuariosVolanterosInactivos
                _domainUsuariosActivosInScreen.value = listaDeUsuariosVolanterosActivos

                withContext(Dispatchers.IO) {
                    Thread.sleep(1000)
                }

                _volanterosActivos.value = listaDeUsuariosVolanterosActivos.isEmpty()
                _status.value = CloudRequestStatus.DONE

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