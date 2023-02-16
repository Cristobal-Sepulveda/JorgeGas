package com.example.conductor.data.network

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/** API to communicate the server with this app, the response is a String,
 *  and then i convert it to a NetworkAsteroidContainer in the method
 *  parseAsteroidsJsonResults in NetworkUtils.kt*/

interface DistanceMatrixApiService {
    @GET("json")
    suspend fun getDistance(
        @Query("origins") origin: String,
        @Query("destinations") destination: String,
        @Query("key") apiKey: String
    ): DistanceMatrixResponse

}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

object DistanceMatrixApi{
    private val retrofitDistanceMatrix = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("https://maps.googleapis.com/maps/api/distancematrix/")
            .build()

    val RETROFIT_SERVICE_DISTANCE_MATRIX: DistanceMatrixApiService by lazy{
        retrofitDistanceMatrix.create(DistanceMatrixApiService::class.java)
    }
}

data class DistanceMatrixResponse(
    val rows: List<DistanceMatrixRow>
)

data class DistanceMatrixRow(
    val elements: List<DistanceMatrixElement>
)

data class DistanceMatrixElement(
    val distance: Distance?
)

data class Distance(
    val text: String
)


