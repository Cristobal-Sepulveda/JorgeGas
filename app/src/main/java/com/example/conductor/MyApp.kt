package com.example.conductor

import android.app.Application
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.AppRepository
import com.example.conductor.data.app_database.getDatabase
import com.example.conductor.ui.administrarcuentas.AdministrarCuentasViewModel
import com.example.conductor.ui.asistencia.AsistenciaViewModel
import com.example.conductor.ui.detallevolantero.DetalleVolanteroViewModel
import com.example.conductor.ui.estadoactual.EstadoActualViewModel
import com.example.conductor.ui.gestiondematerial.GestionDeMaterialViewModel
import com.example.conductor.ui.vistageneral.VistaGeneralViewModel
import com.example.conductor.ui.map.MapViewModel
import com.example.conductor.ui.gestiondevolanteros.GestionDeVolanterosViewModel
import com.example.conductor.ui.registrodeasistencia.RegistroDeAsistenciaViewModel
import com.example.conductor.ui.registrovolanteros.RegistroVolanterosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        /**
         * using Koin Library as a service locator
         */
        val myModule = module {

            //Declare singleton definitions to be later injected using by inject()
            single {
                MapViewModel(
                    get(),
                    get() as AppDataSource
                )
            }
            single {
                VistaGeneralViewModel(
                    get(),
                    get() as AppDataSource
                )
            }
            single {
                GestionDeVolanterosViewModel(
                    get(),
                    get() as AppDataSource
                )
            }
            single {
                AdministrarCuentasViewModel(
                    get(),
                    get() as AppDataSource
                )
            }
            single {
                DetalleVolanteroViewModel(
                    get(),
                    get() as AppDataSource
                )
            }
            single {
                RegistroVolanterosViewModel(
                    get(),
                    get() as AppDataSource
                )
            }
            single{
                EstadoActualViewModel(
                    get(),
                    get() as AppDataSource
                )
            }
            single{
                AsistenciaViewModel(
                    get(),
                    get() as AppDataSource
                )
            }
            single{
                RegistroDeAsistenciaViewModel(
                    get(),
                    get() as AppDataSource
                )
            }
            single{
                GestionDeMaterialViewModel(
                    get(),
                    get() as AppDataSource
                )
            }
            //LOCAL_DATABASE, here im creating the local database in the first start and
            // after that, the db instance persist on the User phone, even if he close the app
            single { getDatabase(this@MyApp).usuarioDao }

            single { getDatabase(this@MyApp).latLngYHoraActualDao }

            single { getDatabase(this@MyApp).jwtDao }

            //REPOSITORY
            single { AppRepository(get(),get(),get()) as AppDataSource }
        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }

/*        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager*/

        //delayedInit()

    }
}

/*    private fun delayedInit() {
        applicationScope.launch{
            setupRecurringWork_fieldUpdate()
            setupRecurringWork_calendarUpdate()
        }
    }

    private fun setupRecurringWork_fieldUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setRequiresDeviceIdle(true)
                }
            }.build()

        val repeatingRequest = PeriodicWorkRequestBuilder<updatingFIELD_DBO_IN_APP_DATABASE>(15, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            updatingFIELD_DBO_IN_APP_DATABASE.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest)
    }

    private fun setupRecurringWork_calendarUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setRequiresDeviceIdle(true)
                }
            }.build()

        val repeatingRequest = PeriodicWorkRequestBuilder<updatingCalendar_inAllFields_toNextDay_inCLOUDFIRESTORE>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            updatingCalendar_inAllFields_toNextDay_inCLOUDFIRESTORE.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest)
    }*/

