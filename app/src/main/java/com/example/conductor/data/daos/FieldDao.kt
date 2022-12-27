package com.example.conductor.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.conductor.data.data_objects.DBO.FIELD_DBO

@Dao
interface FieldDao {

    /**
     * Insert a FIELD in the APP_DATABASE. If the FIELD already exists, replace it.
     *
     * @param field is the FIELD to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveField(field: FIELD_DBO)

    /**
     * @param field_id the id of the FIELD
     * @return the field object with the field_id
     */
    @Query("select * from FIELD_DBO where id = :field_id")
    fun getField(field_id: String): FIELD_DBO

    /**
     * @return all fields.
     */
    @Query("select * from FIELD_DBO")
    fun getFields(): List<FIELD_DBO>

    /**
     * Delete all fields.
     */
    @Query("delete from FIELD_DBO")
    fun deleteFields()
}