package com.example.conductor.ui.map

import android.app.Application
import android.util.Log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.conductor.ui.estadoactual.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.dbo.UsuarioDBO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


enum class CloudDownloadComplete{LOADING, ERROR, DONE}
class MapViewModel(val app: Application, val dataSource: AppDataSource) : BaseViewModel(app) {


    var usuarioEstaActivo = false
    var usuarioDesdeSqlite = ""
    var usuarioRolDesdeSqlite = ""



    suspend fun editarEstadoVolantero(estaActivo: Boolean):Boolean{
        if(dataSource.editarEstadoVolantero(estaActivo)){
            usuarioEstaActivo = estaActivo
            return true
        }
        return false
    }

    suspend fun obtenerUsuariosDesdeSqlite() {
        val usuario = dataSource.obtenerUsuariosDesdeSqlite()
        if(usuario.isNotEmpty()){
            Log.i("VistaGeneralViewModel", "$usuario")
            Log.i("VistaGeneralViewModel", "isNotEmpty")
            usuarioDesdeSqlite = "${usuario.first().nombre} ${usuario.first().apellidos}"
            usuarioRolDesdeSqlite = usuario.first().rol
        }else{
            Log.i("VistaGeneralViewModel", "$usuario")
            Log.i("VistaGeneralViewModel", "isEmpty")
        }
    }

}
