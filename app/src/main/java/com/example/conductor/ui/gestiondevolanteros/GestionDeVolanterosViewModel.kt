package com.example.conductor.ui.gestiondevolanteros

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GestionDeVolanterosViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {
    private val _domainUsuariosInScreen = MutableLiveData<List<Usuario>>()
    val domainUsuariosInScreen: LiveData<List<Usuario>>
        get() = _domainUsuariosInScreen

    fun displayUsuariosInRecyclerView(){
        viewModelScope.launch{
            val listaDeUsuarios = dataSource.obtenerUsuariosDesdeFirestore()
            val listaDeUsuariosVolanteros = mutableListOf<Usuario>()
            for(usuario in listaDeUsuarios){
                if(usuario.rol == "Volantero"){
                    listaDeUsuariosVolanteros.add(usuario)
                }
            }
            Log.i("asd", "$listaDeUsuariosVolanteros")
            listaDeUsuariosVolanteros.sortedWith(compareBy { it.nombre })
            Log.i("asd", "$listaDeUsuariosVolanteros")
            _domainUsuariosInScreen.value = listaDeUsuariosVolanteros

        }
    }

}