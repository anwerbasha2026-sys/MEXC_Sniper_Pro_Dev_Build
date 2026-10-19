package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Mexc24HrTicker(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "priceChange") val priceChange: String? = null,
    @Json(name = "priceChangePercent") val priceChangePercent: String? = null,
    @Json(name = "lastPrice") val lastPrice: String = "0",
    @Json(name = "highPrice") val highPrice: String? = null,
    @Json(name = "lowPrice") val lowPrice: String? = null,
    @Json(name = "volume") val volume: String? = null,
    @Json(name = "quoteVolume") val quoteVolume: String? = null,
    @Json(name = "openTime") val openTime: Long? = null,
    @Json(name = "closeTime") val closeTime: Long? = null
) {
    val lastPriceDouble: Double get() = lastPrice.toDoubleOrNull() ?: 0.0
    val changePercentDouble: Double get() = (priceChangePercent?.toDoubleOrNull() ?: 0.0) * 100.0
    val highDouble: Double get() = highPrice?.toDoubleOrNull() ?: 0.0
    val lowDouble: Double get() = lowPrice?.toDoubleOrNull() ?: 0.0
    val volumeDouble: Double get() = volume?.toDoubleOrNull() ?: 0.0
    val quoteVolumeDouble: Double get() = quoteVolume?.toDoubleOrNull() ?: 0.0
}

@JsonClass(generateAdapter = true)
data class MexcPriceTicker(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "price") val price: String
)

@JsonClass(generateAdapter = true)
data class MexcServerTime(
    @Json(name = "serverTime") val serverTime: Long
)

@JsonClass(generateAdapter = true)
data class MexcBalance(
    @Json(name = "asset") val asset: String,
    @Json(name = "free") val free: String,
    @Json(name = "locked") val locked: String
)

@JsonClass(generateAdapter = true)
data class MexcAccountResponse(
    @Json(name = "canTrade") val canTrade: Boolean? = true,
    @Json(name = "balances") val balances: List<MexcBalance>? = null
)

@JsonClass(generateAdapter = true)
data class MexcOrderResponse(
    @Json(name = "symbol") val symbol: String? = null,
    @Json(name = "orderId") val orderId: String? = null,
    @Json(name = "price") val price: String? = null,
    @Json(name = "origQty") val origQty: String? = null,
    @Json(name = "executedQty") val executedQty: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "side") val side: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "transactTime") val transactTime: Long? = null,
    @Json(name = "msg") val msg: String? = null,
    @Json(name = "code") val code: Int? = null
)
