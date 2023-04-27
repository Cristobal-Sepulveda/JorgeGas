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
    suspend fun obtenerTodoElRegistroTrayectoVolanteros(): MutableList<Any>
    suspend fun obtenerRegistroDiariosDelVolantero(id: String): Any
    suspend fun editarEstadoVolantero(estaActivo: Boolean): Boolean
    suspend fun guardarUsuarioEnSqlite(usuario: UsuarioDBO)
    suspend fun eliminarUsuariosEnSqlite()
    suspend fun obtenerUsuariosDesdeSqlite(): List<UsuarioDBO>
    suspend fun obtenerDistanciaEntreLatLngs(origin: String, destination: String, apiKey: String): DistanceMatrixResponse
    suspend fun registroTrayectoVolanterosEstaActivoFalse(id: String)
    suspend fun actualizarFotoDePerfilEnFirestoreYRoom(fotoPerfil: String): Boolean
    suspend fun guardarLatLngYHoraActualEnRoom(latLngYHoraActualEnRoom: LatLngYHoraActualDBO):Boolean
    suspend fun obtenerLatLngYHoraActualesDeRoom(): List<LatLngYHoraActualDBO>
    suspend fun guardarLatLngYHoraActualEnFirestore(): Boolean
    suspend fun obtenerRegistroDiariosRoomDesdeFirestore(): List<DocumentSnapshot>
    suspend fun solicitarTokenDeSesion(): String
    suspend fun eliminarTokenDeSesion()
    suspend fun validarTokenDeSesion(): Boolean
    suspend fun guardandoTokenDeFCMEnFirestore(): Boolean
    suspend fun eliminandoTokenDeFCMEnFirestore(): Boolean
    suspend fun registrarIngresoDeJornada(latitude: Double, longitude: Double): Boolean
    suspend fun registrarSalidaDeJornada():Boolean
    suspend fun obtenerRegistroDeAsistenciaDeUsuario(): MutableList<AsistenciaIndividual>
    suspend fun avisarQueQuedeSinMaterial()
    suspend fun notificarQueSeAbastecioAlVolanteroDeMaterial(id:String):Boolean
    suspend fun exportarRegistroDeAsistenciaAExcel(mes:String, anio: String)

    suspend fun obtenerRegistroDeAsistenciaYMostrarloComoExcel(mes: String, anio: String): MutableList<Asistencia>

    suspend fun generarInstanciaDeEnvioRegistroDeTrayecto()

    suspend fun obtenerEnvioRegistroDeTrayecto(): List<EnvioRegistroDeTrayectoDBO>
    suspend fun cambiarValorDeEnvioRegistroDeTrayecto(boolean:Boolean)
    suspend fun eliminarInstanciaDeEnvioRegistroDeTrayecto()
    suspend fun agregarBonoPersonalAlVolantero(bono: String, volanteroId: String, mes:String, anio:String): Boolean
}