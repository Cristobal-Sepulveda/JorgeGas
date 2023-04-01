package com.example.conductor.utils

import com.example.conductor.utils.Constants.JWTAPI_URL
import org.json.JSONObject
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
        .baseUrl(JWTAPI_URL)
        .build()

    val RETROFIT_SERVICE_TOKEN: JwtApiService by lazy{
        retrofitJwt.create(JwtApiService::class.java)
    }
}
