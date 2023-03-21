package com.example.conductor.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.conductor.data.data_objects.dbo.LatLngYHoraActualDBO
import com.example.conductor.data.data_objects.dbo.UsuarioDBO

@Dao
interface LatLngYHoraActualDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarLatLngYHoraActual(latLngYHoraActualDBO: LatLngYHoraActualDBO)

    @Query("select * from LatLngYHoraActualDBO")
    fun obtenerLatLngYHoraActuales(): List<LatLngYHoraActualDBO>

    @Query("delete from LatLngYHoraActualDBO")
    fun eliminarLatLngYHoraActuales()

}