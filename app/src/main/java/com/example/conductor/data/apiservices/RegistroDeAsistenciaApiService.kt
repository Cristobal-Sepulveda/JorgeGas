package com.example.conductor.data.apiservices

import com.example.conductor.data.apiservices.MoshiProvider.moshiConverterFactory
import com.example.conductor.data.data_objects.dto.ApiResponseRdm
import com.example.conductor.data.data_objects.dto.ApiResponseRegistroDeAsistencia
import com.example.conductor.utils.Constants.backend_url
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*

interface RegistroDeAsistenciaApiService {

    @POST("exportarRegistroDeAsistenciaAExcel")
    fun exportarRegistroDeAsistenciaAExcel(
        @Header("mes") desde: String,
        @Header("anio") hasta:String
    ): Call<ResponseBody>

}

object RegistroDeAsistenciaApi{
    private val retrofitRegistroDeAsistencia = Retrofit.Builder()
        .addConverterFactory(moshiConverterFactory)
        .baseUrl(backend_url)
        .build()

    val RETROFIT_SERVICE_REGISTRODEASISTENCIA: RegistroDeAsistenciaApiService by lazy{
        retrofitRegistroDeAsistencia.create(RegistroDeAsistenciaApiService::class.java)
    }
}
