package com.example.conductor.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.conductor.data.data_objects.dbo.EnvioRegistroDeTrayectoDBO
import com.example.conductor.data.data_objects.dbo.LatLngYHoraActualDBO
import com.example.conductor.data.data_objects.dbo.UsuarioDBO
import com.example.conductor.data.data_objects.domainObjects.Asistencia
import com.example.conductor.data.data_objects.domainObjects.AsistenciaIndividual
import com.example.conductor.data.data_objects.domainObjects.RegistroTrayectoVolantero
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.data.network.DistanceMatrixResponse
import com.google.firebase.firestore.DocumentSnapshot

interface AppDataSource {
    suspend fun obtenerUsuariosDesdeFirestore(): MutableList<Usuario>
    suspend fun obtenerRegistroTrayectoVolanteros(): MutableList<RegistroTrayectoVolantero>
    suspend fun obtenerRegistroTrayectoVolanterosColRef(): List<DocumentSnapshot>
    suspend fun ingresarUsuarioAFirestore(usuario:Usuario):Boolean
    suspend fun eliminarUsuarioDeFirebase(usuario: Usuario)
    suspend fun obtenerRolDelUsuarioActual(): String
    suspend fun obtenerTodoElRegistroTrayectoVolanteros(context:Context): MutableList<Any>
    suspend fun obtenerRegistroDiariosDelVolantero(id: String, context: Context): Any
    suspend fun editarEstadoVolantero(estaActivo: Boolean): Boolean
    suspend fun guardarUsuarioEnSqlite(usuario: UsuarioDBO)
    suspend fun eliminarUsuariosEnSqlite()
    suspend fun obtenerUsuariosDesdeSqlite(): List<UsuarioDBO>
    suspend fun obtenerDistanciaEntreLatLngs(origin: String, destination: String, apiKey: String): DistanceMatrixResponse
    suspend fun registroTrayectoVolanterosEstaActivoFalse(id: String, context: Context)
    suspend fun actualizarFotoDePerfilEnFirestoreYRoom(fotoPerfil: String, context: Context): Boolean
    suspend fun guardarLatLngYHoraActualEnRoom(latLngYHoraActualEnRoom: LatLngYHoraActualDBO):Boolean

    suspend fun obtenerLatLngYHoraActualesDeRoom(): List<LatLngYHoraActualDBO>
    suspend fun guardarLatLngYHoraActualEnFirestore(context: Context): Boolean
    suspend fun obtenerRegistroDiariosRoomDesdeFirestore(context: Context): List<DocumentSnapshot>
    suspend fun solicitarTokenDeSesion(context: Context): String
    suspend fun eliminarTokenDeSesion()
    suspend fun validarTokenDeSesion(): Boolean


    suspend fun guardandoTokenDeFCMEnFirestore(): Boolean
    suspend fun eliminandoTokenDeFCMEnFirestore(): Boolean

    suspend fun registrarIngresoDeJornada(context: Context,
                                          latitude: Double,
                                          longitude: Double): Boolean
    suspend fun registrarSalidaDeJornada(context: Context):Boolean

    suspend fun obtenerRegistroDeAsistencia(context: Context): MutableList<Map<*,*>>

    suspend fun obtenerRegistroDeAsistenciaDeUsuario(context: Context): MutableList<AsistenciaIndividual>

    suspend fun avisarQueQuedeSinMaterial(context: Context)

    suspend fun notificarQueSeAbastecioAlVolanteroDeMaterial(context: Context, id:String)

    suspend fun exportarRegistroDeAsistenciaAExcel(context: Context, desde:String, hasta: String)

    suspend fun obtenerExcelDelRegistroDeAsistenciaDesdeElBackendYParcearloALista(context:Context, desde: String, hasta: String): MutableList<Asistencia>

    suspend fun generarInstanciaDeEnvioRegistroDeTrayecto()

    suspend fun obtenerEnvioRegistroDeTrayecto(): List<EnvioRegistroDeTrayectoDBO>
    suspend fun cambiarValorDeEnvioRegistroDeTrayecto(boolean:Boolean)
    suspend fun eliminarInstanciaDeEnvioRegistroDeTrayecto()
}