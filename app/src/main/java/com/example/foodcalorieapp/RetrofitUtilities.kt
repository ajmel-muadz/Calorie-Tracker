// Credits...
/* -------------------------------------------------------------------------------- */
// Below documentation helped with learning how to use Retrofit's headers.
// 1. https://square.github.io/retrofit/
/* -------------------------------------------------------------------------------- */


package com.example.foodcalorieapp

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

data class Items(
    @SerializedName("items")
    val items: List<Item>
)
data class Item(
    @SerializedName("name")
    val name: String,

    @SerializedName("serving_size_g")
    val servingSize: Double,

    @SerializedName("calories")
    val calories: Double,

    @SerializedName("fat_total_g")
    val fat: Double,

    @SerializedName("protein_g")
    val protein: Double,

    @SerializedName("carbohydrates_total_g")
    val carbs: Double
)

interface RemoteApiCalls {
    @Headers("X-Api-Key:QPNKjFn+JfNv/NeUh77SDw==n94QImJcQ0VIERen")
    @GET("/v1/nutrition")
    suspend fun getItemsList(
        @Query("query") nutritionInfo: String
    ): Items
}

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.calorieninjas.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: RemoteApiCalls by lazy {
        retrofit.create(RemoteApiCalls::class.java)
    }
}

