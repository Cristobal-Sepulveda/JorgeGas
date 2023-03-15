package com.example.conductor.data.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.conductor.data.data_objects.dbo.UsuarioDBO

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarUsuario(usuario: UsuarioDBO)

    @Query("select * from UsuarioDBO")
    fun obtenerUsuarios(): List<UsuarioDBO>

    @Query("delete from UsuarioDBO")
    fun eliminarUsuarios()

    @Query("UPDATE UsuarioDBO SET fotoPerfil =:fotoPerfil WHERE id IN (SELECT id FROM UsuarioDBO ORDER BY id ASC LIMIT 1)")
    fun actualizarFotoPerfil(fotoPerfil: String)
}