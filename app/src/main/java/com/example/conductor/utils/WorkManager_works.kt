/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.conductor.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.DTO.FIELD_DTO
import com.example.conductor.data.data_objects.DTO.asDataBaseModel
import com.example.conductor.utils.Constants.firebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/*class updatingFIELD_DBO_IN_APP_DATABASE(appContext: Context, params: WorkerParameters):
    CoroutineWorker(appContext, params), KoinComponent {

    companion object {
        const val WORK_NAME = "RefreshingFieldsBetween15days"
    }

    val dataSource: AppDataSource by inject()
    val cloudDB = FirebaseFirestore.getInstance()

    *//**
     * A coroutine-friendly method to manage the work that i want to do.
     * Note: In recent work version upgrade, 1.0.0-alpha12 and onwards have a breaking change.
     * The doWork() function now returns Result instead of Payload because they have combined Payload into Result.
     * Read more here - https://developer.android.com/jetpack/androidx/releases/work#1.0.0-alpha12*
     *//*
    override suspend fun doWork(): Result {
        return try {
            //This method will update the comunas saved in the database where the user plays, to get
            // the latest list every 15 days.
            getingComunasInDb()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }

    }

    private suspend fun getingComunasInDb() {
        //constants
        val listOfFields = dataSource.gettingFieldsFromDatabase()
        val comunasToUpdate: ArrayList<String> = arrayListOf()
        //with this i get all the comunas in the localdb
        if (!listOfFields.isNullOrEmpty()) {
            var i = 0
            comunasToUpdate.add(listOfFields[i].comuna)
            i++
            while (i < listOfFields.size) {
                if (!comunasToUpdate.contains(listOfFields[i].comuna)) {
                    comunasToUpdate.add(listOfFields[i].comuna)
                }
                i++
            }
            //here, i will ask to the cloud to return me all the documents with that comunas. 1 by 1.
            updatingLocalDatabase(comunasToUpdate)
        }

    }

    //here, i will ask to the cloud to return me all the documents with that comunas. 1 by 1.
    private suspend fun updatingLocalDatabase(comunasToUpdate: ArrayList<String>) {
        var i = 0
        val comunasToUpdateSize = comunasToUpdate.size
        while (i < comunasToUpdateSize) {
            cloudDB.collection("FIELD_S")
                .whereEqualTo("comuna", comunasToUpdate[i])
                .get()

                .addOnSuccessListener {

                    GlobalScope.launch(Dispatchers.IO) {
                        dataSource.deletingSavedFieldsInLocalDatabase()
                    }

                    for (document in it) {
                        val singleFieldId = document.get("id")
                        val singleFieldComuna = document.get("comuna")
                        val singleFieldInfo = document.get("field_info") as ArrayList<*>
                        val fieldDTO = FIELD_DTO(
                            singleFieldInfo[0] as String,
                            singleFieldInfo[1] as String,
                            singleFieldComuna as String,
                            (singleFieldInfo[2] as GeoPoint).latitude,
                            (singleFieldInfo[2] as GeoPoint).longitude,
                            singleFieldId as String
                        )
                        GlobalScope.launch(Dispatchers.IO) {
                            dataSource.savingFieldToLocalDatabase(fieldDTO.asDataBaseModel(fieldDTO))
                        }
                    }
                    i++
                }


                .addOnFailureListener{
                    GlobalScope.launch(Dispatchers.IO) {
                        updatingLocalDatabase(comunasToUpdate)
                    }
                }
        }
    }
}*/

/*class updatingCalendar_inAllFields_toNextDay_inCLOUDFIRESTORE(appContext: Context, params: WorkerParameters):
    CoroutineWorker(appContext, params), KoinComponent{

    companion object {
        const val WORK_NAME = "updatingFieldsCalendars_inCLOUDFIRESTORE"
    }

    val cloudDB = FirebaseFirestore.getInstance()

    *//**
     * A coroutine-friendly method to manage the work that i want to do.
     * Note: In recent work version upgrade, 1.0.0-alpha12 and onwards have a breaking change.
     * The doWork() function now returns Result instead of Payload because they have combined Payload into Result.
     * Read more here - https://developer.android.com/jetpack/androidx/releases/work#1.0.0-alpha12*
     *//*
    override suspend fun doWork(): Result {
        return try {
            //HERE IM OVERRITING THE CLOUD DATABASE TO DELETE THE DAY OF TODAY AND CREATING A NEW ONe
                // AT THE END OF THE CALENDAR. THIS IS BECAUSE THE APP ONLY CAN SHOW 14 days of calendar
                    //per field. This only is doing by my mobile. THIS WILL UPDATE THIS DATA AVAILABLE
                        // FOR ALL USERS 1 TIME PER DAY. its a script.
            if (firebaseAuth.currentUser!!.email!! == "sepulveda.cristobal.ignacio@gmail.com") {
                val days = getFourteenDaysDatesFromToday()
                GlobalScope.launch(Dispatchers.IO) {
                    var i = 0
                    //here is 2 because we only have 2 fields in db
                    while (i < 2) {
                        val request = cloudDB.collection("FIELD_S")
                            .document("$i")
                            .collection("CALENDARIO")
                            .get()
                        request.addOnSuccessListener {
                            var count = 0
                            while (count<13 && i <2) {
                                updateCalendarLastDay_fromAField(i, count, days[count], it.documents[count + 1])
                                count++
                            }
                            if(i<2){
                                updateCalendarLastDay_fromAField(i,count,days[count],it.documents[count])
                                i++
                            } else{
                                return@addOnSuccessListener
                            }
                        }
                        request.addOnFailureListener {
                            Result.retry()
                        }
                    }
                }
            }
            Result.success()
        }
        catch(e:Exception){
            Result.retry()
        }
    }

    fun updateCalendarLastDay_fromAField(i: Int, count: Int, day: String, hashMap: DocumentSnapshot) {
        val rightNow = Calendar.getInstance()
        val hour = rightNow[Calendar.HOUR_OF_DAY]
        val minute = rightNow[Calendar.MINUTE]
        val seconds = rightNow[Calendar.SECOND]
        val list = hashMapOf(
            "0" to "",
            "1" to "",
            "2" to "",
            "3" to "",
            "4" to "",
            "5" to "",
            "6" to "",
            "7" to "",
            "8" to "",
            "9" to ""
        )
        Log.i("Launched", "$i $count")
        val updateRequest = cloudDB.collection("FIELD_S")
            .document("$i")
            .collection("CALENDARIO")

        if (count != 13) {
            updateRequest.document("$count").update("0", hashMap.get("0"))
            updateRequest.document("$count").update("1", hashMap.get("1"))
            updateRequest.document("$count").update("2", hashMap.get("2"))
            updateRequest.document("$count").update("3", hashMap.get("3"))
            updateRequest.document("$count").update("4", hashMap.get("4"))
            updateRequest.document("$count")
                .update("current_hour", "${hour}:${minute}:${seconds}")
            updateRequest.document("$count").update("id", day)
        }
        else{
            updateRequest.document("$count").update("0", list)
            updateRequest.document("$count").update("1", list)
            updateRequest.document("$count").update("2", list)
            updateRequest.document("$count").update("3", list)
            updateRequest.document("$count").update("4", list)
            updateRequest.document("$count").update("id", day)
            updateRequest.document("$count").update("current_hour",
                "${hour}:${minute}:${seconds}")
        }
    }

    fun getFourteenDaysDatesFromToday(): ArrayList<String> {
        val formattedDateList = ArrayList<String>()
        val calendar = Calendar.getInstance()
        for (i in 0..13) {
            val currentTime = calendar.time
            *//*val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())*//*
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            formattedDateList.add(dateFormat.format(currentTime))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return formattedDateList
    }

}*/

