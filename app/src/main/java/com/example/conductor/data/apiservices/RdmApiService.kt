package com.example.conductor.data.apiservices

import com.example.conductor.data.apiservices.MoshiProvider.moshiConverterFactory
import com.example.conductor.data.data_objects.dto.ApiResponseRdm
import com.example.conductor.utils.Constants.backend_url
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*

interface RdmApiService {
    @POST("rdmPedido")
    fun rdmPedido(@Header("uid") uid: String): Call<ApiResponseRdm>

    @POST("rdmEntrega")
    fun rdmEntrega(@Header("uid") id: String): Call<ApiResponseRdm>

}
object RdmApi{
    private val retrofitRdm = Retrofit.Builder()
        .addConverterFactory(moshiConverterFactory)
        .baseUrl(backend_url)
        .build()

    val RETROFIT_SERVICE_RDM: RdmApiService by lazy{
        retrofitRdm.create(RdmApiService::class.java)
    }
}
