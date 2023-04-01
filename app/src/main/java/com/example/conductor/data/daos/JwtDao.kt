package com.example.conductor.data.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.conductor.data.data_objects.dbo.JwtDBO

@Dao
interface JwtDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarJwt(jwt: JwtDBO)

    @Query("select * from JwtDBO")
    fun obtenerJwt(): List<JwtDBO>

    @Query("delete from JwtDBO")
    fun eliminarJwt()

}