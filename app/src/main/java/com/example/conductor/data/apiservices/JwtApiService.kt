package com.example.conductor.data.apiservices

import com.example.conductor.utils.Constants.backend_url
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface JwtApiService {
    @GET("getToken")
    fun getToken(): Call<String>

    /*@POST("validateToken")
    fun validateToken(
        @Query("token") token: String
    ): Call<String>*/

    @POST("validateToken")
    fun validateToken(@Header("Authorization") token: String): Call<String>



}
object JwtApi{
    private val retrofitJwt = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(backend_url)
        .build()

    val RETROFIT_SERVICE_TOKEN: JwtApiService by lazy{
        retrofitJwt.create(JwtApiService::class.java)
    }
}
