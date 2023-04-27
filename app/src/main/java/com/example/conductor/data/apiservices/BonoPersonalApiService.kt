package com.example.conductor.data.apiservices

import com.example.conductor.data.apiservices.MoshiProvider.moshiConverterFactory
import com.example.conductor.data.data_objects.dto.ApiResponse
import com.example.conductor.data.data_objects.dto.ApiResponseRdm
import com.example.conductor.utils.Constants.backend_url
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*

interface BonoPersonalApiService {

    @POST("ingresarBono")
    fun ingresarBono(
        @Header("uid") uid: String,
        @Header("bono") bono: String,
        @Header("mes") mes: String,
        @Header("anio") anio: String,
    ): Call<ApiResponse>

}

object BonoPersonalApi{
    private val retrofitBonoPersonal = Retrofit.Builder()
        .addConverterFactory(moshiConverterFactory)
        .baseUrl(backend_url)
        .build()

    val RETROFIT_SERVICE_BONOPERSONAL: BonoPersonalApiService by lazy{
        retrofitBonoPersonal.create(BonoPersonalApiService::class.java)
    }
}
