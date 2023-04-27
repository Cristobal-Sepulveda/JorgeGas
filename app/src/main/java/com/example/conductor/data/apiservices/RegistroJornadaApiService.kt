package com.example.conductor.data.apiservices

import com.example.conductor.data.apiservices.MoshiProvider.moshiConverterFactory
import com.example.conductor.data.data_objects.dto.ApiResponse
import com.example.conductor.data.data_objects.dto.JornadaRequest
import com.example.conductor.utils.Constants.backend_url
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*

interface RegistroJornadaApiService {
    @POST("ingresoJornada")
    fun ingresoJornada(@Body request: JornadaRequest): Call<ApiResponse>

    @POST("salidaJornada")
    fun salidaJornada(
        @Header("id") id: String,
        @Header("tiempoEnVerde") tiempoEnVerde: String,
        @Header("tiempoEnAmarillo") tiempoEnAmarillo: String,
        @Header("tiempoEnRojo") tiempoEnRojo: String,
        @Header("tiempoEnAzul") tiempoEnAzul: String,
        @Header("tiempoEnRosado") tiempoEnRosado: String,
    ): Call<ApiResponse>

}
object RegistroJornadaApi{
    private val retrofitRegistroJornada = Retrofit.Builder()
        .addConverterFactory(moshiConverterFactory)
        .baseUrl(backend_url)
        .build()

    val RETROFIT_SERVICE_REGISTRO_JORNADA: RegistroJornadaApiService by lazy{
        retrofitRegistroJornada.create(RegistroJornadaApiService::class.java)
    }
}
