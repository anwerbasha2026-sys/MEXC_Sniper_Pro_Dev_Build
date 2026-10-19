package com.example.data.model

data class KlineCandle(
    val openTime: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val closeTime: Long
) {
    val isBullish: Boolean get() = close >= open
    val bodyTop: Double get() = maxOf(open, close)
    val bodyBottom: Double get() = minOf(open, close)

    companion object {
        fun fromMexcArray(raw: List<Any>): KlineCandle? {
            return try {
                val openTime = (raw[0] as? Number)?.toLong() ?: (raw[0] as? String)?.toLongOrNull() ?: 0L
                val open = raw[1].toString().toDoubleOrNull() ?: 0.0
                val high = raw[2].toString().toDoubleOrNull() ?: 0.0
                val low = raw[3].toString().toDoubleOrNull() ?: 0.0
                val close = raw[4].toString().toDoubleOrNull() ?: 0.0
                val volume = raw[5].toString().toDoubleOrNull() ?: 0.0
                val closeTime = (raw[6] as? Number)?.toLong() ?: (raw[6] as? String)?.toLongOrNull() ?: 0L
                KlineCandle(openTime, open, high, low, close, volume, closeTime)
            } catch (e: Exception) {
                null
            }
        }
    }
}
