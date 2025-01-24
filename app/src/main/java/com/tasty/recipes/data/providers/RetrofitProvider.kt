package com.tasty.recipes.data.providers

import com.tasty.recipes.services.MessageService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitProvider {

    companion object {

        private const val BASE_URL = "http://10.0.2.2:8080/recipes/"

        fun getRetrofit(): MessageService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(MessageService::class.java)
        }
    }
}