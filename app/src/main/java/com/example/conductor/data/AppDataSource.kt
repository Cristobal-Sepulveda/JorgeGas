package com.example.conductor.data

import com.example.conductor.data.data_objects.domainObjects.Usuario

interface AppDataSource {
    suspend fun obtenerUsuariosDesdeFirestore(): MutableList<Usuario>
    suspend fun ingresarUsuarioAFirestore(usuario:Usuario)
    suspend fun eliminarUsuarioDeFirebase(usuario: Usuario)
    suspend fun obtenerRolDelUsuarioActual(): String
    suspend fun observarTrayectoVolanteros()
    suspend fun editarEstadoVolantero(estaActivo: Boolean)
}