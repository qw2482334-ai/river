package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

data class ExchangeRateResponse(
    @Json(name = "result") val result: String = "",
    @Json(name = "base_code") val baseCode: String = "CNY",
    @Json(name = "rates") val rates: Map<String, Double> = emptyMap(),
    @Json(name = "time_last_update_utc") val timeLastUpdate: String = ""
)

interface ExchangeRateApi {
    @GET("v6/latest/{base}")
    suspend fun getLatestRates(@Path("base") baseCurrency: String = "CNY"): ExchangeRateResponse
}

class CurrencyService {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://open.er-api.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val exchangeApi = retrofit.create(ExchangeRateApi::class.java)

    // Fallback static exchange rates relative to 1 CNY
    private val fallbackRates = mapOf(
        "CNY" to 1.0,
        "USD" to 0.138,
        "EUR" to 0.128,
        "JPY" to 21.2,
        "HKD" to 1.08,
        "GBP" to 0.109,
        "SGD" to 0.186,
        "KRW" to 191.5
    )

    suspend fun fetchLiveRates(baseCurrency: String = "CNY"): Result<Map<String, Double>> = withContext(Dispatchers.IO) {
        try {
            val response = exchangeApi.getLatestRates(baseCurrency)
            if (response.rates.isNotEmpty()) {
                Result.success(response.rates)
            } else {
                Result.success(fallbackRates)
            }
        } catch (e: Exception) {
            // Fallback to cached default rates if offline
            Result.success(fallbackRates)
        }
    }

    fun convertCurrency(amount: Double, fromRate: Double, toRate: Double): Double {
        if (fromRate == 0.0) return 0.0
        return (amount / fromRate) * toRate
    }
}
