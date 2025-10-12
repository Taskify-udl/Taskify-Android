package com.taskify.taskify_android.data.network

import android.content.Context
import com.taskify.taskify_android.data.models.auth.AuthPreferences

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitInstance {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    // Interceptor que afegeix la clau API (si existeix)
    private class AuthInterceptor(private val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val token = AuthPreferences.getTokenBlocking(context)
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Token $token")
                    .build()
            } else {
                chain.request()
            }
            return chain.proceed(request)
        }
    }

    fun getApi(context: Context): ApiService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
