package com.example.conductor.data.source

import android.content.Context
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.dbo.UsuarioDBO
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.data.network.DistanceMatrixResponse

class FakeDataSource : AppDataSource{
    override suspend fun obtenerUsuariosDesdeFirestore(): MutableList<Usuario> {
        TODO("Not yet implemented")
    }

    override suspend fun ingresarUsuarioAFirestore(usuario: Usuario) {
        TODO("Not yet implemented")
    }

    override suspend fun eliminarUsuarioDeFirebase(usuario: Usuario) {
        TODO("Not yet implemented")
    }

    override suspend fun obtenerRolDelUsuarioActual(): String {
        TODO("Not yet implemented")
    }

    override suspend fun obtenerRegistroTrayectoVolanteros(): Any {
        TODO("Not yet implemented")
    }

    override suspend fun obtenerTodoElRegistroTrayectoVolanteros(context: Context): MutableList<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun obtenerRegistroDelVolantero(id: String): Any {
        TODO("Not yet implemented")
    }

    override suspend fun editarEstadoVolantero(estaActivo: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun guardarUsuarioEnSqlite(usuario: UsuarioDBO) {
        TODO("Not yet implemented")
    }

    override suspend fun eliminarUsuariosEnSqlite() {
        TODO("Not yet implemented")
    }

    override suspend fun obtenerUsuariosDesdeSqlite(): List<UsuarioDBO> {
        TODO("Not yet implemented")
    }

    override suspend fun obtenerDistanciaEntreLatLngs(
        origin: String,
        destination: String,
        apiKey: String
    ): DistanceMatrixResponse {
        TODO("Not yet implemented")
    }

    override suspend fun registroTrayectoVolanterosEstaActivoFalse(id: String, context: Context) {
        TODO("Not yet implemented")
    }

}