package com.example.conductor.utils

import com.example.conductor.utils.Constants.backend_url
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

data class JornadaRequest(
    val id: String,
    val nombreCompleto: String,
    val latitude: Double,
    val longitude: Double
)

data class ApiResponse(
    val msg: String
)
val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
val moshiConverterFactory = MoshiConverterFactory.create(moshi)


interface RegistroJornadaApiService {
    @POST("ingresoJornada")
    fun ingresoJornada(@Body request: JornadaRequest): Call<ApiResponse>

    @POST("salidaJornada")
    fun salidaJornada(@Header("id") id: String): Call<ApiResponse>

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
