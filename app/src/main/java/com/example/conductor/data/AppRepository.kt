package com.example.conductor.data

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.example.conductor.data.daos.UsuarioDao
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

@Suppress("LABEL_NAME_CLASH")
class AppRepository(private val usuarioDao: UsuarioDao,
                    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO): AppDataSource {

    private val cloudDB = FirebaseFirestore.getInstance()

    override suspend fun obtenerUsuariosDesdeFirestore(): MutableList<Usuario> = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                val listAux = mutableListOf<Usuario>()
                try{
                    val colRef = cloudDB.collection("Usuarios").get().await()
                    for (document in colRef){
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
                    return@withContext listAux
                }catch(ex: Exception){
                    Log.i("AppRepository", ex.message!!)
                    return@withContext listAux
                }
            }
        }
    }

    override suspend fun ingresarUsuarioAFirestore(usuario: Usuario){
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
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

    //este solo devuelve los activos. hay que cambiarle el nombre
    override suspend fun obtenerRegistroTrayectoVolanteros(): Any = withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                try {
                    val colRef = cloudDB.collection("RegistroTrayectoVolanteros").get().await()
                    val registroTrayectoVolantero = mutableListOf<RegistroTrayectoVolantero>()
                    for (document in colRef){
                        registroTrayectoVolantero.add(RegistroTrayectoVolantero(
                            document.id, document.get("estaActivo") as Boolean)
                        )
                    }
                    return@withContext registroTrayectoVolantero
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
                    Toast.makeText(context, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
                    deferred.complete(registroTrayectoVolantero)
                }
            return@withContext deferred.await()
        }
    }



    override suspend fun obtenerRegistroDelVolantero(id: String): Any = withContext(ioDispatcher){
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                try{
                    return@withContext cloudDB.collection("RegistroTrayectoVolanteros")
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
                try{
                    cloudDB.collection("RegistroTrayectoVolanteros")
                    .document(firebaseAuth.currentUser!!.uid)
                    .update("estaActivo", estaActivo)
                    return@withContext true
                }catch(e:Exception){
                    return@withContext false
                }
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
}
