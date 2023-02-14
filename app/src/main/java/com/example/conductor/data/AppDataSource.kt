package com.example.conductor.data

import com.example.conductor.data.data_objects.dbo.UsuarioDBO
import com.example.conductor.data.data_objects.domainObjects.Usuario

interface AppDataSource {
    suspend fun obtenerUsuariosDesdeFirestore(): MutableList<Usuario>
    suspend fun ingresarUsuarioAFirestore(usuario:Usuario)
    suspend fun eliminarUsuarioDeFirebase(usuario: Usuario)
    suspend fun obtenerRolDelUsuarioActual(): String
    suspend fun obtenerRegistroTrayectoVolanteros(): Any
    suspend fun obtenerRegistroDelVolantero(id: String): Any
    suspend fun editarEstadoVolantero(estaActivo: Boolean): Boolean
    suspend fun guardarUsuarioEnSqlite(usuario: UsuarioDBO)
    suspend fun eliminarUsuarioEnSqlite()
    suspend fun obtenerUsuariosDesdeSqlite(): List<UsuarioDBO>
}