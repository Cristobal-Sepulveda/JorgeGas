package com.example.conductor.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.conductor.data.data_objects.dbo.UsuarioDBO
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.data.network.DistanceMatrixResponse

interface AppDataSource {
    suspend fun obtenerUsuariosDesdeFirestore(): MutableList<Usuario>
    suspend fun ingresarUsuarioAFirestore(usuario:Usuario):Boolean
    suspend fun eliminarUsuarioDeFirebase(usuario: Usuario)
    suspend fun obtenerRolDelUsuarioActual(): String
    suspend fun obtenerRegistroTrayectoVolanteros(): Any
    suspend fun obtenerTodoElRegistroTrayectoVolanteros(context:Context): MutableList<Any>
    suspend fun obtenerRegistroDelVolantero(id: String): Any
    suspend fun editarEstadoVolantero(estaActivo: Boolean): Boolean
    suspend fun guardarUsuarioEnSqlite(usuario: UsuarioDBO)
    suspend fun eliminarUsuariosEnSqlite()
    suspend fun obtenerUsuariosDesdeSqlite(): List<UsuarioDBO>
    suspend fun obtenerDistanciaEntreLatLngs(origin: String, destination: String, apiKey: String): DistanceMatrixResponse
    suspend fun registroTrayectoVolanterosEstaActivoFalse(id: String, context: Context)
    suspend fun actualizarFotoDePerfilEnFirestoreYRoom(fotoPerfil: String, context: Context): Boolean

}