package com.example.conductor.ui.gestiondematerial

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import com.example.conductor.ui.base.BaseViewModel
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.launch

class GestionDeMaterialViewModel(val app: Application, val dataSource: AppDataSource) : BaseViewModel(app) {

    private val _status =MutableLiveData<CloudRequestStatus>()
    val status: LiveData<CloudRequestStatus>
        get() = _status

    private val _hayVolanterosSinMaterial = MutableLiveData<Boolean>()
    val hayVolanterosSinMaterial: LiveData<Boolean>
        get() = _hayVolanterosSinMaterial

    private val _domainUsuariosInScreen = MutableLiveData<List<Usuario>>()
    val domainUsuariosInScreen: LiveData<List<Usuario>>
        get() = _domainUsuariosInScreen

    fun displayVolanterosInRecyclerView(context:Context) {
        _status.value = CloudRequestStatus.LOADING
        Log.d("bindingAdapter", "${status.value}")
        viewModelScope.launch {
            val colRef = dataSource.obtenerUsuariosDesdeFirestore()
            if (colRef.isEmpty()) {
                _status.value = CloudRequestStatus.ERROR
                Log.d("bindingAdapter", "${status.value}")
            } else {
                val iterator = colRef.iterator()
                while (iterator.hasNext()) {
                    val usuario = iterator.next()
                    if (usuario.rol != "Volantero") {
                        iterator.remove()
                    }
                }
                filtrarVolanterosEnRecyclerViewSegunSiTienenMaterial(context, colRef)
            }
        }
    }

    suspend fun filtrarVolanterosEnRecyclerViewSegunSiTienenMaterial(context: Context, listOfVolanteros: MutableList<Usuario>){
        Log.i("filtrarVolanterosEnRecyclerViewSegunSiTienenMaterial", "filtrarVolanterosEnRecyclerViewSegunSiTienenMaterial")
        val registroTrayectoVolanteros = dataSource.obtenerTodoElRegistroTrayectoVolanteros(context)
        val listaFinal = mutableListOf<Usuario>()
        if(registroTrayectoVolanteros.isEmpty()) {
            _status.value = CloudRequestStatus.ERROR
        }else{
            registroTrayectoVolanteros.forEach{ document ->
                val aux = document as DocumentSnapshot
                if(aux.data?.contains("conMaterial") ==true && aux.data?.get("conMaterial") == false){
                    Log.i("filtrarVolanterosEnRecyclerViewSegunSiTienenMaterial", "volantero sin material")
                    listOfVolanteros.forEach { usuario ->
                        if (usuario.id == aux.id) {
                            listaFinal.add(usuario)
                        }
                    }
                }
            }
            _domainUsuariosInScreen.value = listaFinal
            _hayVolanterosSinMaterial.value = listaFinal.isNotEmpty()
            _status.value = CloudRequestStatus.DONE
        }
    }

    suspend fun notificarQueSeAbastecioAlVolanteroDeMaterial(context: Context, id:String){
        _status.value = CloudRequestStatus.LOADING
        if(dataSource.notificarQueSeAbastecioAlVolanteroDeMaterial(context, id)) {
            displayVolanterosInRecyclerView(context)
        }else{
            _status.value = CloudRequestStatus.ERROR
        }
    }

    fun vaciarRecyclerView(){
        _domainUsuariosInScreen.value = mutableListOf()
    }

    fun resetearHayVolanterosSinMaterial(){
        _hayVolanterosSinMaterial.value = true
    }
}