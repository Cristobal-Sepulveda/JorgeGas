package com.example.conductor.data

import com.example.conductor.data.data_objects.DBO.FIELD_DBO
import com.example.conductor.data.data_objects.DBO.PERMISSION_DENIED_DBO
import com.example.conductor.data.data_objects.domainObjects.Usuario

interface AppDataSource {

    /**
     * Methods to interact with the local database
     * */
    suspend fun savingFieldToLocalDatabase(field: FIELD_DBO)
    suspend fun gettingFieldsFromDatabase(): List<FIELD_DBO>
    suspend fun deletingSavedFieldsInLocalDatabase()
    suspend fun registrarIntentoDeObtenerPermisos(permissionDenied: PERMISSION_DENIED_DBO)
    suspend fun obtenerIntentoDePermisos(): List<PERMISSION_DENIED_DBO>
    suspend fun obtenerUsuariosDesdeFirestore(): MutableList<Usuario>
    suspend fun ingresarUsuarioAFirestore(usuario:Usuario)
    suspend fun eliminarUsuarioDeFirebase(usuario: Usuario)
}