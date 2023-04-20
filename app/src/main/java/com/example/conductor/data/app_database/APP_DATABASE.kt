package com.example.conductor.data.app_database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.conductor.data.daos.EnvioRegistroDeTrayectoDao
import com.example.conductor.data.daos.JwtDao
import com.example.conductor.data.daos.LatLngYHoraActualDao
import com.example.conductor.data.daos.UsuarioDao
import com.example.conductor.data.data_objects.dbo.EnvioRegistroDeTrayectoDBO
import com.example.conductor.data.data_objects.dbo.JwtDBO
import com.example.conductor.data.data_objects.dbo.LatLngYHoraActualDBO
import com.example.conductor.data.data_objects.dbo.UsuarioDBO
import com.example.conductor.data.data_objects.domainObjects.RegistroTrayectoVolantero


/**
 * Here is the instance of the APP_DATABASE and the method that create the DB when the user start
 * the app.
 */
@Database(
    entities = [
        UsuarioDBO::class,
        LatLngYHoraActualDBO::class,
        JwtDBO::class,
        EnvioRegistroDeTrayectoDBO::class,
    ],
    version = 2,
    exportSchema = false
)

abstract class LOCAL_DATABASE: RoomDatabase() {
    abstract val usuarioDao: UsuarioDao
    abstract val latLngYHoraActualDao: LatLngYHoraActualDao
    abstract val jwtDao: JwtDao
    abstract val envioRegistroDeTrayectoDao: EnvioRegistroDeTrayectoDao
}

private lateinit var INSTANCE: LOCAL_DATABASE

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Migration code from version 1 to version 2
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS EnvioRegistroDeTrayectoDBO(" +
                    "envioElRegistro INTEGER NOT NULL," +
                    "id TEXT PRIMARY KEY NOT NULL " +
                    ")"
        )
    }
}

fun getDatabase(context: Context): LOCAL_DATABASE {
    synchronized(LOCAL_DATABASE::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext, LOCAL_DATABASE::class.java, "database")
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
    return INSTANCE
}