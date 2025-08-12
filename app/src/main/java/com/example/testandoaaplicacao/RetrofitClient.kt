package com.example.testandoaaplicacao

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {


 //   private const val BASE_URL = "https://seu-servidor-aqui.com/api/"


    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }


    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()


    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-dev.routino.io/smart-route/create-user-steps.json/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        retrofit.create(ApiService::class.java)
    }
}
