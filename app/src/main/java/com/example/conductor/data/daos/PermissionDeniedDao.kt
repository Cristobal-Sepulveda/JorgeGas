package com.example.conductor.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.conductor.data.data_objects.DBO.PERMISSION_DENIED_DBO

@Dao
interface PermissionDeniedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePermission(field: PERMISSION_DENIED_DBO)

    @Query("select * from PERMISSION_DENIED_DBO")
    fun getPermission(): List<PERMISSION_DENIED_DBO>

    @Query("delete from FIELD_DBO")
    fun deleteFields()
}