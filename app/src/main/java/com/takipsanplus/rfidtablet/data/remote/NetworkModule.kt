package com.takipsanplus.rfidtablet.data.remote

import com.google.gson.GsonBuilder
import com.takipsanplus.rfidtablet.data.model.consignment.CloseConsignmentResponseModel
import com.takipsanplus.rfidtablet.data.model.consignment.CloseConsignmentResponseModelDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = "http://api.takipsanplus.com/"

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()

    private val gson = GsonBuilder()
        .registerTypeAdapter(
            CloseConsignmentResponseModel::class.java,
            CloseConsignmentResponseModelDeserializer()
        )
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()

    val authApi: AuthApiService = retrofit.create(AuthApiService::class.java)

    val consignmentApi: ConsignmentApiService = retrofit.create(ConsignmentApiService::class.java)

    val packagesApi: PackagesApiService = retrofit.create(PackagesApiService::class.java)
}
