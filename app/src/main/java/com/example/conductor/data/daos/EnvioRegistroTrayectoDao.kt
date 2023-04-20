package com.example.conductor.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.conductor.data.data_objects.dbo.EnvioRegistroDeTrayectoDBO

@Dao
interface EnvioRegistroDeTrayectoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarEnvioRegistroDeTrayecto(envioRegistroDeTrayectoDBO: EnvioRegistroDeTrayectoDBO)

    @Query("select * from EnvioRegistroDeTrayectoDBO")
    fun obtenerEnvioRegistroDeTrayecto(): List<EnvioRegistroDeTrayectoDBO>

    @Query("delete from EnvioRegistroDeTrayectoDBO")
    fun eliminarEnvioRegistroDeTrayecto()

    @Query("UPDATE EnvioRegistroDeTrayectoDBO SET envioElRegistro =:booleano WHERE id IN (SELECT id FROM EnvioRegistroDeTrayectoDBO ORDER BY id ASC LIMIT 1)")
    fun actualizarBooleano(booleano: Boolean)

}