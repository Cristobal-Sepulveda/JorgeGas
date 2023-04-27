package com.example.conductor.data

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.conductor.R
import com.example.conductor.data.apiservices.*
import com.example.conductor.data.daos.EnvioRegistroDeTrayectoDao
import com.example.conductor.data.daos.JwtDao
import com.example.conductor.data.daos.LatLngYHoraActualDao
import com.example.conductor.data.daos.UsuarioDao
import com.example.conductor.data.data_objects.dbo.EnvioRegistroDeTrayectoDBO
import com.example.conductor.data.data_objects.dbo.JwtDBO
import com.example.conductor.data.data_objects.dbo.LatLngYHoraActualDBO
import com.example.conductor.data.data_objects.dbo.UsuarioDBO
import com.example.conductor.data.data_objects.domainObjects.Asistencia
import com.example.conductor.data.data_objects.domainObjects.AsistenciaIndividual
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
import com.example.conductor.data.data_objects.dto.JornadaRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.HttpException
import retrofit2.await
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate


@Suppress("LABEL_NAME_CLASH")
class AppRepository(private val context: Context,
                    private val usuarioDao: UsuarioDao,
                    private val latLngYHoraActualDao: LatLngYHoraActualDao,
                    private val jwtDao: JwtDao,
                    private val envioRegistroDeTrayectoDao: EnvioRegistroDeTrayectoDao,
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
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al obtener los datos.", Toast.LENGTH_LONG).show()
                        deferred.complete(emptyList())
                    }
                    .addOnSuccessListener{
                        deferred.complete(it.documents)
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
    override suspend fun obtenerRegistroDiariosDelVolantero(id: String,context: Context): Any = withContext(ioDispatcher){
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                val deferred = CompletableDeferred<Any>()
                cloudDB.collection("RegistroDiariosDeVolanteros")
                    .document(id).get()
                    .addOnCompleteListener{
                        if(it.isSuccessful) {
                            Log.i("obtenerRegistroDiariosDelVolantero", it.result.data.toString())
                            if(!it.result.data.isNullOrEmpty()){
                                deferred.complete(it.result!!)
                            }else{
                                Toast.makeText(context, "El volantero no tiene registro diario.", Toast.LENGTH_LONG).show()
                                deferred.complete("Sin Registro")
                            }
                        }else{
                            Log.i("obtenerRegistroDiariosDelVolantero", "it.isNotSuccessfull")
                            Toast.makeText(context, "Error al obtener los datos.", Toast.LENGTH_LONG).show()
                            deferred.complete("Error")
                        }
                    }
                return@withContext deferred.await()
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
                var deboContinuar = true

                if(listOfLatLngsYHoraActualDBO.isEmpty()){
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, R.string.no_tienes_trayecto, Toast.LENGTH_LONG)
                            .show()
                    }
                    deboContinuar = false
                    deferred.complete(false)
                }
                if(deboContinuar){
                    Log.e("asistencia", deboContinuar.toString())
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
                }

                return@withContext deferred.await()
            }
        }
    }

    override suspend fun solicitarTokenDeSesion(context: Context): String = withContext(ioDispatcher){
        wrapEspressoIdlingResource{
            withContext(Dispatchers.IO){
                val deferred = CompletableDeferred<String>()
                try{
                    val token = JwtApi.RETROFIT_SERVICE_TOKEN.getToken().await()
                    jwtDao.guardarJwt(JwtDBO(token))
                    deferred.complete(token)
                }catch(e:Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(context, "Error al solicitar el token de sesión", Toast.LENGTH_LONG).show()
                    }
                    deferred.complete("error")
                }
                return@withContext deferred.await()
            }
        }
    }

    override suspend fun validarTokenDeSesion(): Boolean = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(Dispatchers.IO){
                val deferred = CompletableDeferred<Boolean>()
                try{
                    val token = jwtDao.obtenerJwt().first().token
                    println(token)
                    val isTokenValid = JwtApi.RETROFIT_SERVICE_TOKEN.validateToken(token).await()
                    println(isTokenValid)
                    deferred.complete(true)
                }catch(e: HttpException){
                    println(e.response()?.code())
                    println(e.response()?.errorBody()?.string())
                    if(e.response()?.errorBody()?.string() == "No se proporcionó un token"){
                        print("hola")
                        deferred.complete(true)
                    }
                    else{
                        deferred.complete(true)
                        //jwtDao.eliminarJwt()
                    }
                }
                return@withContext deferred.await()
            }
        }
    }

    override suspend fun eliminarTokenDeSesion() {
        wrapEspressoIdlingResource {
            withContext(Dispatchers.IO) {
                jwtDao.eliminarJwt()
            }
        }
    }

    override suspend fun guardandoTokenDeFCMEnFirestore(): Boolean = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(Dispatchers.IO){
                val deferred = CompletableDeferred<Boolean>()
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        Log.i("MyFirebaseMsgService", "token: $token")
                        if (token == null) {
                            deferred.complete(false)
                            return@addOnSuccessListener
                        }

                        cloudDB.collection("Usuarios")
                            .document(firebaseAuth.currentUser!!.uid).update("tokenDeFCM", token)
                            .addOnSuccessListener{
                                deferred.complete(true)
                            }
                            .addOnFailureListener{
                                deferred.complete(false)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("MyFirebaseMsgService", "Error getting FCM token or updating document", e)
                        deferred.complete(false)
                    }

                deferred.await()
            }

        }
    }

    override suspend fun eliminandoTokenDeFCMEnFirestore(): Boolean = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(Dispatchers.IO){
                val deferred = CompletableDeferred<Boolean>()

                cloudDB.collection("Usuarios")
                    .document(firebaseAuth.currentUser!!.uid).update("tokenDeFCM", "")
                    .addOnSuccessListener {
                        deferred.complete(true)
                    }
                    .addOnFailureListener {
                        deferred.complete(false)
                    }
                deferred.await()
            }
        }
    }
    override suspend fun registrarIngresoDeJornada(context: Context,
                                                   latitude: Double,
                                                   longitude: Double, ): Boolean = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                val deferred = CompletableDeferred<Boolean>()
                try{
                    val nombreCompleto = usuarioDao.obtenerUsuarios().first().nombre + " " + usuarioDao.obtenerUsuarios().first().apellidos
                    val jornadaRequest = JornadaRequest(firebaseAuth.currentUser!!.uid, nombreCompleto, latitude, longitude)

                    val request = RegistroJornadaApi.RETROFIT_SERVICE_REGISTRO_JORNADA.ingresoJornada(jornadaRequest).await()

                    if(envioRegistroDeTrayectoDao.obtenerEnvioRegistroDeTrayecto().isEmpty()){
                        envioRegistroDeTrayectoDao.guardarEnvioRegistroDeTrayecto(EnvioRegistroDeTrayectoDBO(false))
                    }
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            request.msg,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    deferred.complete(true)
                } catch(e: HttpException){
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            "Usted ya inicio la jornada hoy.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    if(e.response()?.errorBody()?.string().toString().contains("Usted ya inicio jornada")){
                        deferred.complete(true)
                    }else{
                        deferred.complete(false)
                    }
                }
                deferred.await()
            }
        }
    }
    override suspend fun obtenerEnvioRegistroDeTrayecto(): List<EnvioRegistroDeTrayectoDBO> = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                val deferred = CompletableDeferred<List<EnvioRegistroDeTrayectoDBO>>()
                deferred.complete(envioRegistroDeTrayectoDao.obtenerEnvioRegistroDeTrayecto())
                return@withContext deferred.await()
            }
        }
    }
    override suspend fun registrarSalidaDeJornada(context: Context): Boolean = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                val deferred = CompletableDeferred<Boolean>()
                try{
                    val request = RegistroJornadaApi.RETROFIT_SERVICE_REGISTRO_JORNADA.salidaJornada(firebaseAuth.currentUser!!.uid).await()
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            request.msg,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    envioRegistroDeTrayectoDao.eliminarEnvioRegistroDeTrayecto()
                    deferred.complete(true)
                } catch(e: HttpException){
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            e.response()?.errorBody()?.string(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    deferred.complete(false)
                }
                deferred.await()
            }
        }
    }
    override suspend fun obtenerRegistroDeAsistenciaDeUsuario(context: Context): MutableList<AsistenciaIndividual> = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                val registroTrayectoVolantero = mutableListOf<AsistenciaIndividual>()
                val deferred = CompletableDeferred<MutableList<AsistenciaIndividual>>()

                cloudDB.collection("RegistroDeAsistencia")
                    .document(firebaseAuth.currentUser!!.uid).get()

                    .addOnFailureListener{
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                "Error al obtener el registro de asistencia",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        deferred.complete(registroTrayectoVolantero)
                    }

                    .addOnSuccessListener{
                        if(it.get("registroAsistencia") == null){
                            registroTrayectoVolantero.add(
                                AsistenciaIndividual(
                                    "error",
                                    "error",
                                    "error"
                                )
                            )
                            deferred.complete(registroTrayectoVolantero)
                            return@addOnSuccessListener
                        }
                        val listado = it.get("registroAsistencia") as MutableList<*>
                        listado.forEach{ registro ->
                            val asistencia = registro as HashMap<*, *>
                            registroTrayectoVolantero.add(
                                AsistenciaIndividual(
                                    asistencia["fecha"] as String,
                                    asistencia["ingresoJornada"] as String,
                                    asistencia["salidaJornada"] as String
                                )
                            )
                        }
                        deferred.complete(registroTrayectoVolantero)
                    }
                deferred.await()
            }
        }
    }
    override suspend fun obtenerRegistroDeAsistencia(context: Context): MutableList<Map<*,*>> = withContext(ioDispatcher){
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                val deferred = CompletableDeferred<MutableList<Map<*,*>>>()

                cloudDB.collection("RegistroDeAsistencia").get()
                    .addOnFailureListener{
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                "Error al obtener el registro de asistencia",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        deferred.complete(mutableListOf())
                    }

                    .addOnSuccessListener {
                        if(it.documents.isEmpty()){
                            deferred.complete(mutableListOf())
                        }else{
                            val listadoDeRegistros = it.documents.map{ document->
                                document.data as Map<String,*>
                            }
                            deferred.complete(listadoDeRegistros.toMutableList())
                        }
                    }
                deferred.await()
            }
        }
    }
    @SuppressLint("MissingPermission")
    override suspend fun exportarRegistroDeAsistenciaAExcel(context: Context, mes: String, anio: String) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                val auxMes: String = when(mes){
                    "Enero" -> "01"
                    "Febrero" -> "02"
                    "Marzo" -> "03"
                    "Abril" -> "04"
                    "Mayo" -> "05"
                    "Junio" -> "06"
                    "Julio" -> "07"
                    "Agosto" -> "08"
                    "Septiembre" -> "09"
                    "Octubre" -> "10"
                    "Noviembre" -> "11"
                    "Diciembre" -> "12"
                    else -> {""}
                }
                val response =
                    RegistroDeAsistenciaApi.RETROFIT_SERVICE_REGISTRODEASISTENCIA.exportarRegistroDeAsistenciaAExcel(
                        auxMes,
                        anio
                    ).execute()

                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes()

                    if (bytes != null) {
                        // Save the Excel file to Downloads folder
                        val filename = "RegistroDeAsistenciaVolanteros-$mes--$anio.xlsx"
                        val mimeType = "application/vnd.ms-excel"
                        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                        // Create a file object for the Excel file
                        val file = File(downloadsFolder, filename)

                        // Write the bytes to the file
                        FileOutputStream(file).use { output -> output.write(bytes) }

                        // Notify the media scanner to add the file to the Downloads folder
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(file.absolutePath),
                            arrayOf(mimeType),
                            null
                        )

                        // Send a notification to indicate that the file was saved
                        val notificationId = 1
                        val notificationTitle = "Registro de Asistencia"
                        val notificationText = "El archivo excel ya ha sido descargado."
                        val notificationChannelId = "my_channel_id"

                        val notificationIntent = Intent(Intent.ACTION_GET_CONTENT)
                        notificationIntent.type = "*/*"
                        notificationIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                        notificationIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, downloadsFolder.toURI())
                        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

                        val contentIntent = PendingIntent.getActivity(
                            context,
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        val notificationBuilder =
                            NotificationCompat.Builder(context, notificationChannelId)
                                .setSmallIcon(R.drawable.icono_app_sin_fondo)
                                .setContentTitle(notificationTitle)
                                .setContentText(notificationText)
                                .setContentIntent(contentIntent)
                                .setAutoCancel(true)

                        val notificationManager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                        val channel = NotificationChannel(
                            notificationChannelId,
                            "My Channel",
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                        channel.description = "My Channel Description"
                        notificationManager.createNotificationChannel(channel)

                        notificationManager.notify(notificationId, notificationBuilder.build())

                        // Display a toast to indicate that the file was saved
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                "El archivo excel ya ha sido descargado.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        // Display a toast to indicate that the file was not downloaded
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                "El servidor no recibio el requerimiento. Intentelo nuevamente.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override suspend fun obtenerRegistroDeAsistenciaYMostrarloComoExcel(context: Context, mes:String, anio: String):
            MutableList<Asistencia> = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                val deferred = CompletableDeferred<MutableList<Asistencia>>()
                val listaDeUsuariosYSuAsistencia = mutableListOf<Asistencia>()
                var diasTrabajados = 0

                cloudDB.collection("RegistroDeAsistencia").get()
                    .addOnFailureListener {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                "Error al obtener el registro de asistencia. Intentelo nuevamente.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        deferred.complete(mutableListOf())
                    }
                    .addOnSuccessListener{
                        it.documents.forEach{ documento ->
                            val format = SimpleDateFormat("dd-MM-yyyy")
                            val uid = documento.id
                            val nombreCompleto = documento.data?.get("nombreCompleto") as String
                            val sueldoDiario = 10000
                            val registroAsistencia = documento.data?.get("registroAsistencia") as List<Map<*,*>>
                            var bonop = "0"
                            val aux = documento["registroDeBonoPersonal"] as? List<Map<*, *>> ?: emptyList()
                            Log.e("aux", aux.toString())
                            if(aux.isNotEmpty()){
                                aux.forEach{
                                    Log.e("aux", "registro: ${it["anio"]}-${it["mes"]} parametros: "+anio+"-"+mes)
                                    Log.e("aux", (it["mes"] == mes && it["anio"] == anio).toString())
                                    if(it["mes"] == mes && it["anio"] == anio){
                                        Log.e("mesConBono", it["mes"].toString())
                                        Log.e("mesConBono", it["bono"].toString())
                                        bonop = it["bono"].toString()
                                    }
                                }

                            }
                            val bonor = 0
                            val auxMes: String = when(mes){
                                "Enero" -> "01"
                                "Febrero" -> "02"
                                "Marzo" -> "03"
                                "Abril" -> "04"
                                "Mayo" -> "05"
                                "Junio" -> "06"
                                "Julio" -> "07"
                                "Agosto" -> "08"
                                "Septiembre" -> "09"
                                "Octubre" -> "10"
                                "Noviembre" -> "11"
                                "Diciembre" -> "12"
                                else -> {""}
                            }

                            registroAsistencia.forEach {
                                val date = format.parse(it["fecha"] as String)
                                if (date!!.month+1 == auxMes.toInt() && date!!.year + 1900 == anio.toInt()) {
                                    diasTrabajados++
                                }
                            }

                            val sueldo = sueldoDiario * diasTrabajados

                            if(diasTrabajados > 0){
                                listaDeUsuariosYSuAsistencia.add(
                                    Asistencia(
                                        uid,
                                        nombreCompleto,
                                        sueldoDiario.toString(),
                                        diasTrabajados.toString(),
                                        sueldo.toString(),
                                        bonop.toString(),
                                        bonor.toString(),
                                        (sueldo + bonop.toInt() + bonor.toInt()).toString()
                                    )
                                )
                            }

                        }
                        if(listaDeUsuariosYSuAsistencia.isEmpty()){
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    context,
                                    "No hay registros de asistencia en el rango de fechas seleccionado.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        deferred.complete(listaDeUsuariosYSuAsistencia)
                    }
                return@withContext deferred.await()
            }
        }
    }

    override suspend fun agregarBonoPersonalAlVolantero(bono: String, volanteroId: String, mes:String, anio:String): Boolean = withContext(ioDispatcher){
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                val deferred = CompletableDeferred<Boolean>()
                try {
                    val request = BonoPersonalApi.RETROFIT_SERVICE_BONOPERSONAL.ingresarBono(volanteroId, bono, mes, anio).await()
                    enviarToastEnBackground(request.msg)
                    deferred.complete(true)
                }catch(e:Exception){
                    enviarToastEnBackground(e.message?: "Error al agregar bono personal al volantero.")
                    deferred.complete(false)
                }
                return@withContext deferred.await()
            }
        }
    }

    override suspend fun avisarQueQuedeSinMaterial(context: Context){
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                //val request = RegistroJornadaApi.RETROFIT_SERVICE_REGISTRO_JORNADA.salidaJornada(firebaseAuth.currentUser!!.uid).await()
                val request = RdmApi.RETROFIT_SERVICE_RDM.rdmPedido(firebaseAuth.currentUser!!.uid).await()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        request.msg,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    override suspend fun notificarQueSeAbastecioAlVolanteroDeMaterial(context: Context, id: String):Boolean = withContext(ioDispatcher){
        withContext(ioDispatcher){
            val deferred = CompletableDeferred<Boolean>()
            try {
                val request = RdmApi.RETROFIT_SERVICE_RDM.rdmEntrega(id).await()
                deferred.complete(true)
                enviarToastEnBackground(request.msg+"\n"+"Espere mientras se actualiza la lista.")
            }catch(e:Exception){
                deferred.complete(false)
                enviarToastEnBackground("Error al notificar al volantero que se le abastecio de material.")
            }
            return@withContext deferred.await()
        }
    }
    override suspend fun generarInstanciaDeEnvioRegistroDeTrayecto() {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                val hayInstancia = envioRegistroDeTrayectoDao.obtenerEnvioRegistroDeTrayecto()
                if(hayInstancia.isEmpty()){
                    envioRegistroDeTrayectoDao.guardarEnvioRegistroDeTrayecto(
                        EnvioRegistroDeTrayectoDBO(
                        false)
                    )
                }
            }
        }
    }
    override suspend fun cambiarValorDeEnvioRegistroDeTrayecto(boolean: Boolean){
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                envioRegistroDeTrayectoDao.actualizarBooleano(boolean)
            }
        }
    }
    override suspend fun eliminarInstanciaDeEnvioRegistroDeTrayecto() {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                envioRegistroDeTrayectoDao.eliminarEnvioRegistroDeTrayecto()
            }
        }
    }
    override suspend fun obtenerLatLngYHoraActualesDeRoom(): List<LatLngYHoraActualDBO> = withContext(ioDispatcher){
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                return@withContext latLngYHoraActualDao.obtenerLatLngYHoraActuales()
            }
        }
    }


    private suspend fun enviarToastEnBackground(message: String){
        withContext(Dispatchers.Main){
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

}
