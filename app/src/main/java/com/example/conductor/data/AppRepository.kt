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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

@Suppress("LABEL_NAME_CLASH")
class AppRepository(private val fieldDao: FieldDao,
                    private val permissionDeniedDao: PermissionDeniedDao,
                    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO): AppDataSource {

    val cloudDB = FirebaseFirestore.getInstance()

    override suspend fun savingFieldToLocalDatabase(field: FIELD_DBO) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                fieldDao.saveField(field)
            }
        }
    }

    override suspend fun gettingFieldsFromDatabase(): List<FIELD_DBO> =  withContext(ioDispatcher){
        wrapEspressoIdlingResource {
            try {
                val list = Result.Success(fieldDao.getFields())
                return@withContext list.data
            } catch (ex: Exception) {
                val listError : List<FIELD_DBO> = listOf()
                Result.Error(ex.localizedMessage)
                return@withContext listError
            }
        }
    }

    override suspend fun deletingSavedFieldsInLocalDatabase() {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                fieldDao.deleteFields()
            }
        }
    }

    override suspend fun registrarIntentoDeObtenerPermisos(permissionDenied: PERMISSION_DENIED_DBO) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                permissionDeniedDao.savePermission(permissionDenied)
            }
        }

    }

    override suspend fun obtenerIntentoDePermisos(): List<PERMISSION_DENIED_DBO> =  withContext(ioDispatcher) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                try {
                    val list = Result.Success(permissionDeniedDao.getPermission())
                    return@withContext list.data
                } catch (ex: Exception) {
                    val listError : List<PERMISSION_DENIED_DBO> = listOf()
                    Result.Error(ex.localizedMessage)
                    return@withContext listError
                }
            }
        }
    }

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
                        )
                        listAux.add(usuario)
                    }
                    return@withContext listAux
                }catch(ex: Exception){
                    Log.i("asd", ex.message!!)
                    return@withContext listAux
                }
            }
        }
    }
}
