package com.tasty.recipes.services

import com.tasty.recipes.data.entities.Message
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MessageService {

    @POST("addRecipe")
    suspend fun sendNotificationService(@Body message: Message): String
}