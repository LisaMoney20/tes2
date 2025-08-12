package com.example.testandoaaplicacao

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("https://api-dev.routino.io/smart-route/create-user-steps.json/")
    suspend fun sendLocation(

        @Body locationData: LocationPostRequest
    ): Response<Unit>
}