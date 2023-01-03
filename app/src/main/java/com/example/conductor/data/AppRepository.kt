package com.example.conductor.data

import com.example.conductor.data.data_objects.DBO.FIELD_DBO
import com.example.conductor.data.daos.FieldDao
import com.example.conductor.data.daos.PermissionDeniedDao
import com.example.conductor.data.data_objects.DBO.PERMISSION_DENIED_DBO
import com.example.conductor.utils.EspressoIdlingResource.wrapEspressoIdlingResource
import com.example.conductor.utils.Result
import kotlinx.coroutines.*

class AppRepository(private val fieldDao: FieldDao,
                    private val permissionDeniedDao: PermissionDeniedDao,
                    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO): AppDataSource {

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

    override suspend fun obtenerIntentoDePermisos(): List<PERMISSION_DENIED_DBO> {
        TODO("Not yet implemented")
    }



}
