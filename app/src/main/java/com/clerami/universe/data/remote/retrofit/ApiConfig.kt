    package com.clerami.universe.data.remote.retrofit

    import android.content.Context
    import com.clerami.universe.BuildConfig
    import okhttp3.Interceptor
    import okhttp3.OkHttpClient
    import okhttp3.Request
    import okhttp3.logging.HttpLoggingInterceptor
    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory
    import java.util.concurrent.TimeUnit

    object ApiConfig {

        fun getApiService(context: Context): ApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }

            val tokenInterceptor = Interceptor { chain ->
                val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("auth_token", "") ?: ""

                val request: Request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                chain.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(tokenInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }