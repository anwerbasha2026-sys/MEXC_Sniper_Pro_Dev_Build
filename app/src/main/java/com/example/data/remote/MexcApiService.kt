package com.example.data.remote

import com.example.data.model.Mexc24HrTicker
import com.example.data.model.MexcAccountResponse
import com.example.data.model.MexcOrderResponse
import com.example.data.model.MexcPriceTicker
import com.example.data.model.MexcServerTime
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface MexcApiService {
    @GET("api/v3/ping")
    suspend fun ping(): Any

    @GET("api/v3/time")
    suspend fun getServerTime(): MexcServerTime

    @GET("api/v3/ticker/24hr")
    suspend fun get24HrTickers(): List<Mexc24HrTicker>

    @GET("api/v3/ticker/24hr")
    suspend fun get24HrTicker(@Query("symbol") symbol: String): Mexc24HrTicker

    @GET("api/v3/ticker/price")
    suspend fun getTickerPrices(): List<MexcPriceTicker>

    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 100
    ): List<List<Any>>

    @GET("api/v3/account")
    suspend fun getAccount(
        @Header("X-MEXC-APIKEY") apiKey: String,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String,
        @Query("recvWindow") recvWindow: Long = 10000L
    ): MexcAccountResponse

    @POST("api/v3/order")
    suspend fun placeOrder(
        @Header("X-MEXC-APIKEY") apiKey: String,
        @Query("symbol") symbol: String,
        @Query("side") side: String,
        @Query("type") type: String,
        @Query("quoteOrderQty") quoteOrderQty: Double? = null,
        @Query("quantity") quantity: Double? = null,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String,
        @Query("recvWindow") recvWindow: Long = 10000L
    ): MexcOrderResponse
}
