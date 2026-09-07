package com.lakhvinder.rbsbot.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface AiHttpApi {
    @POST
    suspend fun post(
        @Url url: String,
        @Header("Authorization") authorization: String?,
        @Header("X-Title") xTitle: String?,
        @Body body: RequestBody
    ): retrofit2.Response<okhttp3.ResponseBody>
}

object ApiFactory {
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    private val okHttp = okhttp3.OkHttpClient.Builder()
        .connectTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttp)
        .build()

    val api: AiHttpApi = retrofit.create(AiHttpApi::class.java)

    fun jsonBody(text: String): RequestBody = text.toRequestBody(jsonMedia)
}

class AiApiException(message: String, val code: Int = -1) : Exception(message)
