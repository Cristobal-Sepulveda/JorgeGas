package com.example.conductor.data.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val base_url = "https://webpay3gint.transbank.cl/"
/*https://webpay3gint.transbank.cl/rswebpaytransaction/api*/

/**
*  You can find how to interact with TRANSBANK API Service in the README.md file
*/

interface TransBankApiService{
    //TODO: Add API Call
    @GET("")
    suspend fun getResponse(
        @Query("Tbk-Api-Key-Id") apiKeyId: String,         // Tbk-Api-Key-Id: Código de comercio
        @Query("Tbk-Api-Key-Secret") apiKeySecret: String, // Tbk-Api-Key-Secret: Llave secreta
        @Query("Content-Type") contentType: String         // Content-Type: application/json
    ): Call<String>
}

object TransBankApi {
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(base_url)
        .build()

    val RETROFIT_SERVICE_TRANSBANK_API: TransBankApiService by lazy {
        retrofit.create(TransBankApiService::class.java)
    }
}
