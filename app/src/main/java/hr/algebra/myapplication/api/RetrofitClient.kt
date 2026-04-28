package hr.algebra.myapplication.api

import android.annotation.SuppressLint
import android.content.Context
import hr.algebra.myapplication.api.middleware.AuthInterceptor
import hr.algebra.myapplication.managers.TokenManager
import hr.algebra.myapplication.managers.UserManager
import hr.algebra.myapplication.repository.UserRepository
import hr.algebra.myapplication.repository.VehicleRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080"
    private var appContext: Context? = null
    var userManager: UserManager? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            userManager = UserManager(
                UserRepository(apiService),
                VehicleRepository(apiService)
            )
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()

        appContext?.let {
            val tokenManager = TokenManager(it)
            builder.addInterceptor(AuthInterceptor(tokenManager))
        }

        builder.addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun buildApiService(authInterceptor: AuthInterceptor): ApiService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)    // attaches Bearer token to every request
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

}