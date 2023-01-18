package com.example.conductor.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.conductor.data.data_objects.DBO.FIELD_DBO
import com.example.conductor.data.daos.FieldDao
import com.example.conductor.data.daos.PermissionDeniedDao
import com.example.conductor.data.data_objects.DBO.PERMISSION_DENIED_DBO
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.utils.EspressoIdlingResource.wrapEspressoIdlingResource
import com.example.conductor.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

@Suppress("LABEL_NAME_CLASH")
class AppRepository(private val fieldDao: FieldDao,
                    private val permissionDeniedDao: PermissionDeniedDao,
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
                            document.get("nombre") as String,
                            document.get("apellidoPaterno") as String,
                            document.get("apellidoMaterno") as String,
                            document.get("usuario") as String,
                            document.get("password") as String,
                            document.get("deshabilitada") as Boolean,
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

    override suspend fun obtenerRolDelUsuarioActual(): String {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher){
                try{
                    val user = FirebaseAuth.getInstance().currentUser
                    val docRef = cloudDB.collection("Usuarios").document(user!!.uid).get().await()
                    return@withContext docRef.get("rol") as String
                }catch(e:Exception){
                    return@withContext "Error"
                }
            }
        }
        return "Error"
    }
}
