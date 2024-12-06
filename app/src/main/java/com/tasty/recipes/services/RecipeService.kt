package com.tasty.recipes.services

import com.tasty.recipes.data.entities.RecipeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeService {

    @GET("recipes")
    suspend fun findAll(@Query("limit") limit: Int = 0): Response<RecipeResponse>

   /* @GET("recipes/tag/{country}")
    suspend fun findRecipeByCountry(@Path("country") country:String): Response<RecipeResponse>

    @GET("recipes/{id}")
    suspend fun findRecipeById(@Path("id") id:String): Response<RecipeItemResponse>

    @GET("recipes/search")
    suspend fun findRecipeByName(@Query("q") q:String): Response<RecipeResponse>*/
}