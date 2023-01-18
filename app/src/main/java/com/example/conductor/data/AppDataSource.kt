package com.example.conductor.data

import com.example.conductor.data.data_objects.DBO.FIELD_DBO
import com.example.conductor.data.data_objects.DBO.PERMISSION_DENIED_DBO
import com.example.conductor.data.data_objects.domainObjects.Usuario

interface AppDataSource {

    /**
     * Methods to interact with the local database
     * */
    suspend fun obtenerUsuariosDesdeFirestore(): MutableList<Usuario>
    suspend fun ingresarUsuarioAFirestore(usuario:Usuario)
    suspend fun eliminarUsuarioDeFirebase(usuario: Usuario)
    suspend fun obtenerRolDelUsuarioActual(): String
}