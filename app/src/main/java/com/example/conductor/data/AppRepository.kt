package com.example.conductor.data

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.example.conductor.R
import com.example.conductor.data.daos.LatLngYHoraActualDao
import com.example.conductor.data.daos.UsuarioDao
import com.example.conductor.data.data_objects.dbo.LatLngYHoraActualDBO
import com.example.conductor.data.data_objects.dbo.UsuarioDBO
import com.example.conductor.data.data_objects.domainObjects.RegistroTrayectoVolantero
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.utils.Constants.firebaseAuth
import com.example.conductor.utils.EspressoIdlingResource.wrapEspressoIdlingResource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import com.example.conductor.data.network.DistanceMatrixApi
import com.example.conductor.data.network.DistanceMatrixResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import java.time.LocalDate
import java.time.LocalTime

@Suppress("LABEL_NAME_CLASH")
class AppRepository(private val usuarioDao: UsuarioDao,
                    private val latLngYHoraActualDao: LatLngYHoraActualDao,
                    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO): AppDataSource {

    private val cloudDB = FirebaseFirestore.getInstance()

    override suspend fun obtenerUsuariosDesdeFirestore(): MutableList<Usuario> = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                val deferred = CompletableDeferred<MutableList<Usuario>>()
                val listAux = mutableListOf<Usuario>()

                val colRef = cloudDB.collection("Usuarios").get()

                colRef.addOnSuccessListener{
                    for (document in it){
                        val usuario = Usuario(
                            document.id,
                            document.get("fotoPerfil") as String,
                            document.get("nombre") as String,
                            document.get("apellidos") as String,
                            document.get("telefono") as String,
                            document.get("usuario") as String,
                            document.get("password") as String,
                            document.get("deshabilitada") as Boolean,
                            document.get("sesionActiva") as Boolean,
                            document.get("rol") as String
                        )
                        listAux.add(usuario)
                    }
                    deferred.complete(listAux)
                }
                colRef.addOnFailureListener{
                    deferred.complete(listAux)
                }
                return@withContext deferred.await()
            }
        }
    }

    override suspend fun obtenerRegistroTrayectoVolanteros(): MutableList<RegistroTrayectoVolantero> = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                val deferred = CompletableDeferred<MutableList<RegistroTrayectoVolantero>>()
                val colRef = cloudDB.collection("RegistroTrayectoVolanteros").get()
                colRef.addOnSuccessListener {
                    val registroTrayectoVolantero = mutableListOf<RegistroTrayectoVolantero>()
                    for (document in it) {
                        registroTrayectoVolantero.add(
                            RegistroTrayectoVolantero(
                                document.id, document.get("estaActivo") as Boolean
                            )
                        )
                    }
                    deferred.complete(registroTrayectoVolantero)
                }
                colRef.addOnFailureListener {
                    deferred.complete(mutableListOf())
                }
                return@withContext deferred.await()
            }
        }
    }

    override suspend fun obtenerRegistroTrayectoVolanterosColRef(): List<DocumentSnapshot> = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                val deferred = CompletableDeferred<List<DocumentSnapshot>>()
                cloudDB.collection("RegistroTrayectoVolanteros").get()
                    .addOnSuccessListener{
                        deferred.complete(it.documents)
                    }
                    .addOnFailureListener {
                        deferred.complete(emptyList())
                    }
                return@withContext deferred.await()
            }
        }
    }

    override suspend fun obtenerRegistroDiariosRoomDesdeFirestore(context: Context): List<DocumentSnapshot> = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                val deferred = CompletableDeferred<List<DocumentSnapshot>>()
                cloudDB.collection("RegistroDiariosDeVolanteros").get()
                    .addOnSuccessListener{
                        deferred.complete(it.documents)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al obtener los datos.", Toast.LENGTH_LONG).show()
                        deferred.complete(emptyList())
                    }
                return@withContext deferred.await()
            }
        }
    }

    override suspend fun ingresarUsuarioAFirestore(usuario: Usuario): Boolean = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                val deferred = CompletableDeferred<Boolean>()
                val ingresandoUsuarioAFirestore = cloudDB
                    .collection("Usuarios")
                    .document(usuario.id).set(usuario)
                ingresandoUsuarioAFirestore
                    .addOnSuccessListener {
                        deferred.complete(true)
                    }
                    .addOnFailureListener {
                        deferred.complete(false)
                    }
                return@withContext deferred.await()
            }
        }
    }

    override suspend fun eliminarUsuarioDeFirebase(usuario: Usuario) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                try{
                    cloudDB.collection("Usuarios")
                        .document(usuario.id).set(usuario)
                    return@withContext true
                }catch(e:Exception){
                    return@withContext false
                }
            }
        }
    }

    override suspend fun obtenerRolDelUsuarioActual(): String = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                try {
                    val user = FirebaseAuth.getInstance().currentUser
                    val docRef = cloudDB.collection("Usuarios").document(user!!.uid).get().await()
                    return@withContext docRef.get("rol") as String
                } catch (e: Exception) {
                    return@withContext "Error"
                }
            }
        }
    }
    override suspend fun obtenerTodoElRegistroTrayectoVolanteros(context: Context): MutableList<Any> = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            val colRef = cloudDB.collection("RegistroTrayectoVolanteros")
            val registroTrayectoVolantero = mutableListOf<Any>()
            val deferred = CompletableDeferred<MutableList<Any>>()

            colRef.get()
                .addOnSuccessListener{ querySnapshot ->
                    for (document in querySnapshot.documents) {
                        registroTrayectoVolantero.add(document)
                    }
                    deferred.complete(registroTrayectoVolantero)
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al obtener los datos", Toast.LENGTH_LONG).show()
                    deferred.complete(registroTrayectoVolantero)
                }
            return@withContext deferred.await()
        }
    }
    override suspend fun obtenerRegistroDiariosDelVolantero(id: String): Any = withContext(ioDispatcher){
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                try{
                    return@withContext cloudDB.collection("RegistroDiariosDeVolanteros")
                        .document(id).get().await()
                }catch(e:Exception){
                    return@withContext "Error"
                }
            }
        }
    }
    override suspend fun editarEstadoVolantero(estaActivo: Boolean): Boolean = withContext(ioDispatcher) {
        wrapEspressoIdlingResource{
            withContext(ioDispatcher){
                val deferred = CompletableDeferred<Boolean>()
                cloudDB.collection("RegistroTrayectoVolanteros")
                    .document(firebaseAuth.currentUser!!.uid)
                    .update("estaActivo", estaActivo)
                    .addOnSuccessListener {
                        deferred.complete(true)
                    }
                    .addOnFailureListener {
                        deferred.complete(false)
                    }
                return@withContext deferred.await()
            }
        }
    }

    override suspend fun guardarUsuarioEnSqlite(usuario: UsuarioDBO) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                usuarioDao.guardarUsuario(usuario)
            }
        }
    }
    override suspend fun eliminarUsuariosEnSqlite() {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                usuarioDao.eliminarUsuarios()
            }
        }
    }
    override suspend fun obtenerUsuariosDesdeSqlite(): List<UsuarioDBO> = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                return@withContext usuarioDao.obtenerUsuarios()
            }
        }
    }

    override suspend fun obtenerDistanciaEntreLatLngs(
        origin: String,
        destination: String,
        apiKey: String
    ): DistanceMatrixResponse {
            return DistanceMatrixApi.RETROFIT_SERVICE_DISTANCE_MATRIX.getDistance(
                origin,
                destination,
                apiKey
            )
    }

    override suspend fun registroTrayectoVolanterosEstaActivoFalse(id: String, context: Context) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                val task = cloudDB.collection("RegistroTrayectoVolanteros")
                    .document(id)
                    .update("estaActivo", false)

                task.addOnFailureListener{
                    Toast.makeText(
                        context,
                        "Error al actualizar el estado del volantero",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                task.addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "El estado del volantero a pasado a inactivo",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }

    override suspend fun actualizarFotoDePerfilEnFirestoreYRoom(fotoPerfil: String, context: Context):Boolean = withContext(ioDispatcher){
        wrapEspressoIdlingResource {
            suspendCancellableCoroutine<Boolean> { continuation ->
                val task = cloudDB.collection("Usuarios")
                    .document(firebaseAuth.currentUser!!.uid)
                    .update("fotoPerfil", fotoPerfil)

                task.addOnFailureListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Error al actualizar la foto de perfil", Toast.LENGTH_LONG).show()
                    }
                    continuation.resumeWith(Result.failure(it))
                }
                task.addOnSuccessListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        usuarioDao.actualizarFotoPerfil(fotoPerfil)
                    }
                    continuation.resumeWith(Result.success(true))
                }
            }
        }
    }

    override suspend fun guardarLatLngYHoraActualEnRoom(latLngYHoraActualEnRoom: LatLngYHoraActualDBO): Boolean = withContext(ioDispatcher){
        wrapEspressoIdlingResource {
            withContext(Dispatchers.IO){
                try{
                    latLngYHoraActualDao.guardarLatLngYHoraActual(latLngYHoraActualEnRoom)
                    Log.i("guardarLatLngYHoraActualEnRoom", "guardarLatLngYHoraActualEnRoom")
                    return@withContext true
                }catch(e: Exception){
                    Log.i("guardarLatLngYHoraActualEnRoom", "Error: $e")
                    return@withContext false
                }
            }
        }
    }

    override suspend fun guardarLatLngYHoraActualEnFirestore(context: Context): Boolean = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(Dispatchers.IO){
                val deferred = CompletableDeferred<Boolean>()
                val listOfLatLngsYHoraActualDBO = latLngYHoraActualDao.obtenerLatLngYHoraActuales()
                val usuarioActual = usuarioDao.obtenerUsuarios().first()
                val nombreUsuarioActual = "${usuarioActual.nombre} ${usuarioActual.apellidos}"
                val fechaDeHoy = LocalDate.now().toString()
                val firstRequest = cloudDB.collection("RegistroDiariosDeVolanteros")
                    .document(firebaseAuth.currentUser!!.uid).get()

                firstRequest.addOnSuccessListener{
                    if(it.data != null){
                        val registroJornada = it.data!!["registroJornada"] as ArrayList<Map<String, *>>
                        var aux = 0
                        registroJornada.forEach{
                            if(it["fecha"] == fechaDeHoy){
                                aux = 1
                                var registroLatLngs = it["latLngs"] as MutableList<LatLngYHoraActualDBO>
                                registroLatLngs.clear()
                                registroLatLngs.addAll(listOfLatLngsYHoraActualDBO)
                                val documentActualizado = mapOf(
                                    "nombreCompleto" to nombreUsuarioActual,
                                    "registroJornada" to registroJornada,
                                )

                                cloudDB.collection("RegistroDiariosDeVolanteros")
                                    .document(firebaseAuth.currentUser!!.uid)
                                    .update(documentActualizado)
                                    .addOnSuccessListener{
                                        Toast.makeText(context, R.string.el_registro_diario_se_guardo, Toast.LENGTH_LONG).show()
                                        CoroutineScope(ioDispatcher).launch {
                                            latLngYHoraActualDao.eliminarLatLngYHoraActuales()
                                            Log.i("hola", "addOnSuccessListener")
                                            deferred.complete(true)
                                        }
                                    }
                                    .addOnFailureListener{
                                        Toast.makeText(context, R.string.el_registro_diario_no_se_guardo, Toast.LENGTH_LONG).show()
                                        deferred.complete(false)
                                    }
                            }
                        }
                        if(aux==0){
                            Log.i("hola","hola")
                            registroJornada.add(
                                mapOf(
                                    "fecha" to fechaDeHoy,
                                    "latLngs" to listOfLatLngsYHoraActualDBO
                                )
                            )
                            val documentActualizado = mapOf(
                                "nombreCompleto" to nombreUsuarioActual,
                                "registroJornada" to registroJornada,
                            )

                            cloudDB.collection("RegistroDiariosDeVolanteros")
                                .document(firebaseAuth.currentUser!!.uid)
                                .update(documentActualizado)
                                .addOnSuccessListener{
                                    Toast.makeText(context, R.string.el_registro_diario_se_guardo, Toast.LENGTH_LONG).show()
                                    CoroutineScope(ioDispatcher).launch {
                                        latLngYHoraActualDao.eliminarLatLngYHoraActuales()
                                        deferred.complete(true)
                                    }
                                }
                                .addOnFailureListener{
                                    Toast.makeText(context, R.string.el_registro_diario_no_se_guardo, Toast.LENGTH_LONG).show()
                                    deferred.complete(false)
                                }
                        }

                    }
                    else{
                        cloudDB.collection("RegistroDiariosDeVolanteros")
                            .document(firebaseAuth.currentUser!!.uid)
                            .set(
                                mapOf(
                                    "nombreCompleto" to nombreUsuarioActual,
                                    "registroJornada" to arrayListOf(
                                        mapOf(
                                            "fecha" to fechaDeHoy,
                                            "latLngs" to listOfLatLngsYHoraActualDBO
                                        )
                                    )
                                ),
                            )
                            .addOnSuccessListener{
                                Toast.makeText(context, R.string.el_registro_diario_se_guardo, Toast.LENGTH_LONG).show()
                                CoroutineScope(ioDispatcher).launch {
                                    latLngYHoraActualDao.eliminarLatLngYHoraActuales()
                                    deferred.complete(true)
                                }
                            }
                            .addOnFailureListener{
                                Toast.makeText(context, R.string.el_registro_diario_no_se_guardo, Toast.LENGTH_LONG).show()
                                deferred.complete(false)
                            }
                    }

                }
                firstRequest.addOnFailureListener{
                    deferred.complete(false)
                }
                return@withContext deferred.await()
            }
        }
    }

}
