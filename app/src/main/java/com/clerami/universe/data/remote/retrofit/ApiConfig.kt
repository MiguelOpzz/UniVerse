package com.clerami.universe.data.remote.retrofit

import android.content.Context
import com.clerami.universe.BuildConfig
import com.clerami.universe.data.remote.response.Timestamp
import com.clerami.universe.utils.TimestampDeserializer
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

object ApiConfig {

    fun getApiService(context: Context): ApiService {
        // Create a logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }



        // Create a custom Gson instance with the TimestampDeserializer
        val gson = GsonBuilder()
            .registerTypeAdapter(Timestamp::class.java, TimestampDeserializer())  // Register the TimestampDeserializer
            .create()

        // Build the OkHttpClient with interceptors
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)

            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Build the Retrofit instance using the custom Gson
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))  // Use the custom Gson instance
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
